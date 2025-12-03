/**
 * AI 消息解析工具
 * 用于解析 AI 返回的消息，区分普通文本、工具调用、代码块等
 * 
 * 后端 SSE 消息类型：
 * - ai_response: AI 的文本回复内容
 * - tool_request: AI 请求调用某个工具 [选择工具] xxx
 * - tool_executed: 工具执行完成的结果 [工具调用] xxx
 */

import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

// 消息片段类型
export type MessageSegmentType = 'text' | 'tool-select' | 'tool-call' | 'tool-result' | 'code'

// 工具类型
export type ToolType = 'readFile' | 'readDir' | 'modifyFile' | 'writeFile' | 'deleteFile' | 'searchImages' | 'getIllustration' | 'generateLogo' | 'unknown'

// 工具执行结果状态
export type ToolResultStatus = 'success' | 'warning' | 'error'

// 消息片段接口
export interface MessageSegment {
  type: MessageSegmentType
  content: string
  language?: string           // 代码块语言
  toolAction?: string         // 工具调用动作（如：写入文件、读取文件）
  toolTarget?: string         // 工具调用目标（如：文件路径）
  toolType?: ToolType         // 工具类型标识
  resultStatus?: ToolResultStatus  // 执行结果状态
  beforeContent?: string      // 修改前内容（用于 modifyFile）
  afterContent?: string       // 修改后内容（用于 modifyFile）
  fileContent?: string        // 文件内容（用于 writeFile）
}

// 初始化 markdown-it，配置代码高亮
const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  highlight: function (str: string, lang: string) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(str, { language: lang, ignoreIllegals: true }).value
      } catch (__) {}
    }
    return '' // 使用外部默认转义
  }
})

/**
 * 工具选择模式匹配
 * 匹配格式：[选择工具] 工具名
 */
const TOOL_SELECT_PATTERN = /^\[选择工具\]\s*(.+)$/

/**
 * 工具调用模式匹配
 * 匹配格式：[工具调用] 动作 目标
 */
const TOOL_CALL_PATTERN = /^\[工具调用\]\s*(.+?)\s+(.+)$/

/**
 * 执行结果模式匹配
 * 匹配格式：**执行结果**: xxx
 */
const TOOL_RESULT_PATTERN = /^\*\*执行结果\*\*:\s*(.+)$/

/**
 * 代码块模式匹配
 * 匹配格式：```language ... ```
 */
const CODE_BLOCK_PATTERN = /```(\w*)\n([\s\S]*?)```/g

/**
 * 工具动作到类型的映射
 */
const TOOL_ACTION_MAP: Record<string, ToolType> = {
  '读取文件': 'readFile',
  '读取目录': 'readDir',
  '修改文件': 'modifyFile',
  '写入文件': 'writeFile',
  '删除文件': 'deleteFile',
  '搜索图片': 'searchImages',
  '获取插画': 'getIllustration',
  '生成Logo': 'generateLogo',
}

/**
 * 根据动作名称获取工具类型
 */
function getToolType(action: string): ToolType {
  return TOOL_ACTION_MAP[action] || 'unknown'
}

/**
 * 判断是否为读取类工具（不显示执行结果）
 */
export function isReadTool(toolType: ToolType): boolean {
  return toolType === 'readFile' || toolType === 'readDir'
}

/**
 * 解析执行结果状态
 */
function parseResultStatus(result: string): ToolResultStatus {
  if (result.includes('成功')) {
    return 'success'
  } else if (result.includes('警告') || result.includes('未找到')) {
    return 'warning'
  } else if (result.includes('错误') || result.includes('失败')) {
    return 'error'
  }
  return 'success'
}

/**
 * 解析 AI 消息内容为片段数组
 */
export function parseAIMessage(content: string): MessageSegment[] {
  const segments: MessageSegment[] = []
  
  // 按行分割，识别工具调用
  const lines = content.split('\n')
  let currentText = ''
  let inCodeBlock = false
  let codeBlockLang = ''
  let codeBlockContent = ''
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i]
    
    // 正则匹配代码块开始：允许前缀文本，匹配 ``` 或 ~~~
    // Group 1: 前缀文本
    // Group 2: 围栏 (``` 或 ~~~)
    // Group 3: 语言标识
    const startMatch = line.match(/^(.*?)(\s*)(`{3,}|~{3,})(.*)$/)
    
    // 正则匹配代码块结束：必须是行首（允许缩进）的围栏
    const endMatch = line.trim().match(/^(`{3,}|~{3,})\s*$/)
    
    // 检测代码块开始
    if (!inCodeBlock && startMatch) {
      const prefix = startMatch[1]
      // const fence = startMatch[2] // 未使用
      const lang = startMatch[4].trim()
      
      // 如果有前缀文本，先处理
      if (prefix.trim()) {
        currentText += (currentText ? '\n' : '') + prefix
      }
      
      // 刷新当前的普通文本
      if (currentText.trim()) {
        segments.push(...parseTextWithToolCalls(currentText))
        currentText = ''
      }
      
      inCodeBlock = true
      codeBlockLang = lang
      codeBlockContent = ''
      continue
    }
    
    // 检测代码块结束
    if (inCodeBlock && endMatch) {
      segments.push({
        type: 'code',
        content: codeBlockContent,
        language: codeBlockLang || 'plaintext'
      })
      inCodeBlock = false
      codeBlockLang = ''
      codeBlockContent = ''
      continue
    }
    
    // 在代码块内
    if (inCodeBlock) {
      codeBlockContent += (codeBlockContent ? '\n' : '') + line
      continue
    }
    
    // 普通文本行
    currentText += (currentText ? '\n' : '') + line
  }
  
  // 处理剩余的文本
  if (currentText.trim()) {
    segments.push(...parseTextWithToolCalls(currentText))
  }
  
  // 处理未关闭的代码块
  if (inCodeBlock && codeBlockContent) {
    segments.push({
      type: 'code',
      content: codeBlockContent,
      language: codeBlockLang || 'plaintext'
    })
  }
  
  // 后处理：过滤已完成的工具选择状态
  // 当 [工具调用] 出现后，移除对应的 [选择工具] 片段
  return filterCompletedToolSelects(segments)
}

/**
 * 过滤已完成的工具选择状态
 * 当消息中同时存在 [选择工具] 和 [工具调用] 时，只保留 [工具调用]
 */
function filterCompletedToolSelects(segments: MessageSegment[]): MessageSegment[] {
  // 收集所有已完成的工具调用动作
  const completedActions = new Set<string>()
  for (const segment of segments) {
    if (segment.type === 'tool-call' && segment.toolAction) {
      completedActions.add(segment.toolAction)
    }
  }
  
  // 如果没有已完成的工具调用，直接返回
  if (completedActions.size === 0) {
    return segments
  }
  
  // 过滤掉已完成的工具选择状态
  return segments.filter(segment => {
    if (segment.type === 'tool-select' && segment.toolAction) {
      // 如果这个工具已经有对应的工具调用完成，则移除选择状态
      return !completedActions.has(segment.toolAction)
    }
    return true
  })
}

/**
 * 解析文本中的工具调用
 */
function parseTextWithToolCalls(text: string): MessageSegment[] {
  const segments: MessageSegment[] = []
  const lines = text.split('\n')
  let currentText = ''
  
  for (const line of lines) {
    // 检查工具选择标记 [选择工具] xxx
    const selectMatch = line.match(TOOL_SELECT_PATTERN)
    if (selectMatch) {
      // 先保存之前的普通文本
      if (currentText.trim()) {
        segments.push({
          type: 'text',
          content: currentText.trim()
        })
        currentText = ''
      }
      
      const action = selectMatch[1].trim()
      segments.push({
        type: 'tool-select',
        content: line,
        toolAction: action,
        toolType: getToolType(action)
      })
      continue
    }

    // 检查工具调用标记 [工具调用] xxx
    const toolMatch = line.match(TOOL_CALL_PATTERN)
    if (toolMatch) {
      // 先保存之前的普通文本
      if (currentText.trim()) {
        segments.push({
          type: 'text',
          content: currentText.trim()
        })
        currentText = ''
      }
      
      const action = toolMatch[1]
      const target = toolMatch[2]
      segments.push({
        type: 'tool-call',
        content: line,
        toolAction: action,
        toolTarget: target,
        toolType: getToolType(action)
      })
      continue
    }
    
    // 检查执行结果标记 **执行结果**: xxx
    const resultMatch = line.match(TOOL_RESULT_PATTERN)
    if (resultMatch) {
      // 先保存之前的普通文本
      if (currentText.trim()) {
        segments.push({
          type: 'text',
          content: currentText.trim()
        })
        currentText = ''
      }
      
      const result = resultMatch[1]
      segments.push({
        type: 'tool-result',
        content: result,
        resultStatus: parseResultStatus(result)
      })
      continue
    }
    
    // 普通文本行
    currentText += (currentText ? '\n' : '') + line
  }
  
  // 保存剩余的普通文本
  if (currentText.trim()) {
    segments.push({
      type: 'text',
      content: currentText.trim()
    })
  }
  
  return segments
}

/**
 * 渲染 Markdown 文本为 HTML
 */
export function renderMarkdown(content: string): string {
  return md.render(content)
}

/**
 * 高亮代码
 */
export function highlightCode(code: string, language: string): string {
  if (language && hljs.getLanguage(language)) {
    try {
      return hljs.highlight(code, { language, ignoreIllegals: true }).value
    } catch (e) {
      console.warn('代码高亮失败:', e)
    }
  }
  return escapeHtml(code)
}

/**
 * HTML 转义
 */
function escapeHtml(text: string): string {
  const map: Record<string, string> = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  }
  return text.replace(/[&<>"']/g, m => map[m])
}

/**
 * 获取语言显示名称
 */
export function getLanguageDisplayName(lang: string): string {
  const names: Record<string, string> = {
    'js': 'JavaScript',
    'javascript': 'JavaScript',
    'ts': 'TypeScript',
    'typescript': 'TypeScript',
    'vue': 'Vue',
    'html': 'HTML',
    'css': 'CSS',
    'scss': 'SCSS',
    'less': 'Less',
    'json': 'JSON',
    'xml': 'XML',
    'yaml': 'YAML',
    'yml': 'YAML',
    'md': 'Markdown',
    'markdown': 'Markdown',
    'python': 'Python',
    'py': 'Python',
    'java': 'Java',
    'go': 'Go',
    'rust': 'Rust',
    'c': 'C',
    'cpp': 'C++',
    'csharp': 'C#',
    'cs': 'C#',
    'php': 'PHP',
    'ruby': 'Ruby',
    'swift': 'Swift',
    'kotlin': 'Kotlin',
    'sql': 'SQL',
    'shell': 'Shell',
    'bash': 'Bash',
    'sh': 'Shell',
    'powershell': 'PowerShell',
    'dockerfile': 'Dockerfile',
    'plaintext': '纯文本',
    'text': '纯文本',
  }
  return names[lang.toLowerCase()] || lang.toUpperCase()
}

/**
 * 获取工具调用图标
 */
export function getToolCallIcon(action: string): string {
  const icons: Record<string, string> = {
    '写入文件': '📝',
    '读取文件': '📖',
    '读取目录': '📁',
    '修改文件': '✏️',
    '删除文件': '🗑️',
    '执行命令': '⚡',
    '搜索': '🔍',
    '搜索图片': '🖼️',
    '获取插画': '🎨',
    '生成Logo': '🏷️',
  }
  
  // 模糊匹配
  for (const [key, icon] of Object.entries(icons)) {
    if (action.includes(key)) {
      return icon
    }
  }
  
  return '🔧'
}

/**
 * 获取执行结果状态图标
 */
export function getResultStatusIcon(status: ToolResultStatus): string {
  const icons: Record<ToolResultStatus, string> = {
    'success': '✅',
    'warning': '⚠️',
    'error': '❌',
  }
  return icons[status] || '❓'
}

/**
 * 获取执行结果状态颜色类
 */
export function getResultStatusClass(status: ToolResultStatus): string {
  const classes: Record<ToolResultStatus, string> = {
    'success': 'text-green-400',
    'warning': 'text-yellow-400',
    'error': 'text-red-400',
  }
  return classes[status] || 'text-gray-400'
}

