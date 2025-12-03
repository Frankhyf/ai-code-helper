<template>
  <div class="ai-message-renderer">
    <div
      v-for="(segment, index) in segments"
      :key="index"
      class="message-segment"
    >
      <!-- 普通 Markdown 文本 -->
      <div
        v-if="segment.type === 'text'"
        :class="[
          'markdown-content',
          { 'diff-label diff-label-before': isDiffLabel(segment.content) === 'before' },
          { 'diff-label diff-label-after': isDiffLabel(segment.content) === 'after' }
        ]"
        v-html="renderMarkdown(segment.content)"
      ></div>
      
      <!-- 工具选择中状态（加载中） -->
      <div
        v-else-if="segment.type === 'tool-select'"
        class="tool-select-badge"
      >
        <span class="tool-loading-icon">
          <svg class="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
        </span>
        <span class="tool-select-text">正在调用</span>
        <span class="tool-action">{{ segment.toolAction }}</span>
        <span class="tool-dots">...</span>
      </div>
      
      <!-- 工具调用完成 -->
      <div
        v-else-if="segment.type === 'tool-call'"
        class="tool-call-badge"
        :class="{ 'tool-call-read': isReadTool(segment.toolType) }"
      >
        <span class="tool-icon">{{ getToolCallIcon(segment.toolAction || '') }}</span>
        <span class="tool-action">{{ segment.toolAction }}</span>
        <span class="tool-target">{{ segment.toolTarget }}</span>
      </div>
      
      <!-- 工具执行结果 -->
      <div
        v-else-if="segment.type === 'tool-result'"
        class="tool-result-badge"
        :class="getResultStatusClass(segment.resultStatus)"
      >
        <span class="result-icon">{{ getResultStatusIcon(segment.resultStatus) }}</span>
        <span class="result-text">{{ segment.content }}</span>
      </div>
      
      <!-- 代码块 -->
      <div
        v-else-if="segment.type === 'code'"
        class="code-block"
        :class="{ 'code-block-collapsed': isCollapsed(index) }"
      >
        <div class="code-header">
          <div class="code-header-left">
            <span class="code-language">{{ getLanguageDisplayName(segment.language || 'plaintext') }}</span>
            <span class="code-lines">{{ segment.content.split('\n').length }} 行</span>
          </div>
          <div class="code-header-actions">
            <!-- 折叠/展开按钮 -->
            <button
              v-if="segment.content.split('\n').length > 5"
              class="collapse-button"
              @click="toggleCollapse(index)"
              :title="isCollapsed(index) ? '展开代码' : '折叠代码'"
            >
              <svg v-if="isCollapsed(index)" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
              </svg>
              <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7"></path>
              </svg>
            </button>
            <!-- 复制按钮 -->
            <button
              class="copy-button"
              @click="copyCode(segment.content)"
              :title="copied === index ? '已复制!' : '复制代码'"
            >
              <svg v-if="copied !== index" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"></path>
              </svg>
              <svg v-else class="w-4 h-4 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
              </svg>
            </button>
          </div>
        </div>
        <pre class="code-content" :class="{ 'code-content-collapsed': isCollapsed(index) }"><code v-html="highlightCode(isCollapsed(index) ? getCodePreview(segment.content) : segment.content, segment.language || 'plaintext')"></code></pre>
        <!-- 折叠时显示展开提示 -->
        <div v-if="isCollapsed(index)" class="code-expand-hint" @click="toggleCollapse(index)">
          点击展开完整代码...
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  parseAIMessage,
  renderMarkdown as renderMd,
  highlightCode as highlight,
  getLanguageDisplayName as getLangName,
  getToolCallIcon as getIcon,
  getResultStatusIcon as getStatusIcon,
  getResultStatusClass as getStatusClass,
  isReadTool as checkReadTool,
  type MessageSegment,
  type ToolType,
  type ToolResultStatus
} from '@/utils/messageParser'

const props = defineProps<{
  content: string
}>()

// 解析消息内容
const segments = computed<MessageSegment[]>(() => {
  return parseAIMessage(props.content)
})

// 折叠状态管理
const collapsedBlocks = ref<Set<number>>(new Set())

// 判断代码块是否应该默认折叠（在工具调用上下文中）
const shouldDefaultCollapse = (index: number): boolean => {
  // 检查前面的片段是否包含"替换前"或"替换后"标签
  if (index === 0) return false
  
  const prevSegment = segments.value[index - 1]
  if (prevSegment?.type === 'text') {
    const content = prevSegment.content.toLowerCase()
    return content.includes('替换前') || content.includes('替换后')
  }
  return false
}

// 切换代码块折叠状态
const toggleCollapse = (index: number) => {
  if (collapsedBlocks.value.has(index)) {
    collapsedBlocks.value.delete(index)
  } else {
    collapsedBlocks.value.add(index)
  }
}

// 检查代码块是否折叠
const isCollapsed = (index: number): boolean => {
  // 如果没有明确设置，检查是否应该默认折叠
  if (!collapsedBlocks.value.has(index) && shouldDefaultCollapse(index)) {
    // 首次渲染时不自动折叠，让用户看到内容
    return false
  }
  return collapsedBlocks.value.has(index)
}

// 获取代码块预览（前3行）
const getCodePreview = (code: string): string => {
  const lines = code.split('\n')
  if (lines.length <= 3) return code
  return lines.slice(0, 3).join('\n') + '\n...'
}

// 判断文本是否为"替换前/后"标签
const isDiffLabel = (content: string): 'before' | 'after' | null => {
  const trimmed = content.trim()
  if (trimmed === '替换前：' || trimmed === '替换前:') return 'before'
  if (trimmed === '替换后：' || trimmed === '替换后:') return 'after'
  return null
}

// 复制状态
const copied = ref<number | null>(null)

// 渲染 Markdown
const renderMarkdown = (content: string): string => {
  return renderMd(content)
}

// 高亮代码
const highlightCode = (code: string, language: string): string => {
  return highlight(code, language)
}

// 获取语言显示名称
const getLanguageDisplayName = (lang: string): string => {
  return getLangName(lang)
}

// 获取工具调用图标
const getToolCallIcon = (action: string): string => {
  return getIcon(action)
}

// 获取执行结果状态图标
const getResultStatusIcon = (status?: ToolResultStatus): string => {
  return getStatusIcon(status || 'success')
}

// 获取执行结果状态类
const getResultStatusClass = (status?: ToolResultStatus): string => {
  return getStatusClass(status || 'success')
}

// 判断是否为读取类工具
const isReadTool = (toolType?: ToolType): boolean => {
  return checkReadTool(toolType || 'unknown')
}

// 复制代码
const copyCode = async (code: string) => {
  try {
    await navigator.clipboard.writeText(code)
    const index = segments.value.findIndex(s => s.content === code)
    copied.value = index
    setTimeout(() => {
      copied.value = null
    }, 2000)
  } catch (err) {
    console.error('复制失败:', err)
  }
}
</script>

<style scoped>
.ai-message-renderer {
  font-size: 0.875rem;
  line-height: 1.6;
}

.message-segment {
  margin-bottom: 0.75rem;
}

.message-segment:last-child {
  margin-bottom: 0;
}

/* Markdown 内容样式 */
.markdown-content {
  color: #e5e7eb;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  color: #fff;
  font-weight: 600;
  margin-top: 1rem;
  margin-bottom: 0.5rem;
}

.markdown-content :deep(h1) { font-size: 1.5rem; }
.markdown-content :deep(h2) { font-size: 1.25rem; }
.markdown-content :deep(h3) { font-size: 1.125rem; }

.markdown-content :deep(p) {
  margin-bottom: 0.5rem;
}

.markdown-content :deep(strong) {
  color: #fff;
  font-weight: 600;
}

.markdown-content :deep(em) {
  font-style: italic;
  color: #d1d5db;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin-left: 1.5rem;
  margin-bottom: 0.5rem;
}

.markdown-content :deep(li) {
  margin-bottom: 0.25rem;
}

.markdown-content :deep(ul) {
  list-style-type: disc;
}

.markdown-content :deep(ol) {
  list-style-type: decimal;
}

.markdown-content :deep(a) {
  color: #60a5fa;
  text-decoration: underline;
}

.markdown-content :deep(a:hover) {
  color: #93c5fd;
}

.markdown-content :deep(blockquote) {
  border-left: 3px solid #4b5563;
  padding-left: 1rem;
  margin-left: 0;
  color: #9ca3af;
  font-style: italic;
}

.markdown-content :deep(code) {
  background: rgba(0, 0, 0, 0.3);
  padding: 0.125rem 0.375rem;
  border-radius: 0.25rem;
  font-family: 'Fira Code', 'Monaco', 'Consolas', monospace;
  font-size: 0.8em;
  color: #f472b6;
}

.markdown-content :deep(hr) {
  border: none;
  border-top: 1px solid #374151;
  margin: 1rem 0;
}

/* 工具选择中样式（加载状态） */
.tool-select-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.15) 0%, rgba(245, 158, 11, 0.15) 100%);
  border: 1px solid rgba(251, 191, 36, 0.4);
  border-radius: 0.5rem;
  font-size: 0.8rem;
  color: #fcd34d;
  animation: pulse-border 2s ease-in-out infinite;
}

@keyframes pulse-border {
  0%, 100% {
    border-color: rgba(251, 191, 36, 0.4);
  }
  50% {
    border-color: rgba(251, 191, 36, 0.8);
  }
}

.tool-loading-icon {
  display: flex;
  align-items: center;
  color: #fbbf24;
}

.tool-select-text {
  color: #fcd34d;
  font-size: 0.75rem;
}

.tool-select-badge .tool-action {
  font-weight: 600;
  color: #fbbf24;
}

.tool-dots {
  animation: dots-blink 1.5s infinite;
}

@keyframes dots-blink {
  0%, 20% { opacity: 1; }
  40%, 60% { opacity: 0.3; }
  80%, 100% { opacity: 1; }
}

/* 工具调用完成样式 */
.tool-call-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2) 0%, rgba(147, 51, 234, 0.2) 100%);
  border: 1px solid rgba(59, 130, 246, 0.4);
  border-radius: 0.5rem;
  font-size: 0.8rem;
  color: #93c5fd;
  transition: all 0.2s ease;
}

.tool-call-badge:hover {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.3) 0%, rgba(147, 51, 234, 0.3) 100%);
  border-color: rgba(59, 130, 246, 0.6);
}

/* 读取类工具的简洁样式 */
.tool-call-badge.tool-call-read {
  background: linear-gradient(135deg, rgba(107, 114, 128, 0.15) 0%, rgba(75, 85, 99, 0.15) 100%);
  border-color: rgba(107, 114, 128, 0.4);
  color: #9ca3af;
}

.tool-call-badge.tool-call-read .tool-action {
  color: #9ca3af;
}

.tool-call-badge.tool-call-read .tool-target {
  color: #6b7280;
}

.tool-icon {
  font-size: 1rem;
}

.tool-action {
  font-weight: 600;
  color: #60a5fa;
}

.tool-target {
  color: #a5b4fc;
  font-family: 'Fira Code', 'Monaco', 'Consolas', monospace;
  font-size: 0.75rem;
  background: rgba(0, 0, 0, 0.2);
  padding: 0.125rem 0.375rem;
  border-radius: 0.25rem;
}

/* 工具执行结果样式 */
.tool-result-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.375rem 0.75rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 0.375rem;
  font-size: 0.75rem;
  margin-top: 0.25rem;
}

.tool-result-badge.text-green-400 {
  background: rgba(34, 197, 94, 0.1);
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.tool-result-badge.text-yellow-400 {
  background: rgba(251, 191, 36, 0.1);
  border: 1px solid rgba(251, 191, 36, 0.3);
}

.tool-result-badge.text-red-400 {
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.result-icon {
  font-size: 0.875rem;
}

.result-text {
  font-family: 'Fira Code', 'Monaco', 'Consolas', monospace;
}

/* 差异标签样式（替换前/替换后） */
.diff-label {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 600;
  margin: 0.5rem 0 0.25rem 0;
}

.diff-label-before {
  background: rgba(239, 68, 68, 0.15);
  border-left: 3px solid #ef4444;
  color: #fca5a5;
}

.diff-label-after {
  background: rgba(34, 197, 94, 0.15);
  border-left: 3px solid #22c55e;
  color: #86efac;
}

/* 代码块样式 */
.code-block {
  border-radius: 0.5rem;
  overflow: hidden;
  background: #0d1117;
  border: 1px solid #30363d;
  margin: 0.5rem 0;
  transition: all 0.2s ease;
}

.code-block-collapsed {
  border-color: #21262d;
}

.code-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.5rem 1rem;
  background: #161b22;
  border-bottom: 1px solid #30363d;
}

.code-header-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.code-header-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.code-language {
  font-size: 0.75rem;
  font-weight: 500;
  color: #8b949e;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.code-lines {
  font-size: 0.7rem;
  color: #6e7681;
  background: rgba(110, 118, 129, 0.2);
  padding: 0.125rem 0.375rem;
  border-radius: 0.25rem;
}

.collapse-button {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.25rem;
  background: transparent;
  border: none;
  border-radius: 0.25rem;
  color: #8b949e;
  cursor: pointer;
  transition: all 0.2s ease;
}

.collapse-button:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.copy-button {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.25rem;
  background: transparent;
  border: none;
  border-radius: 0.25rem;
  color: #8b949e;
  cursor: pointer;
  transition: all 0.2s ease;
}

.copy-button:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.code-content {
  margin: 0;
  padding: 1rem;
  overflow-x: auto;
  font-family: 'Fira Code', 'Monaco', 'Consolas', 'Liberation Mono', monospace;
  font-size: 0.8rem;
  line-height: 1.5;
  color: #c9d1d9;
  background: transparent;
  transition: max-height 0.3s ease;
}

.code-content-collapsed {
  max-height: 100px;
  overflow: hidden;
  position: relative;
}

.code-content-collapsed::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40px;
  background: linear-gradient(transparent, #0d1117);
  pointer-events: none;
}

.code-content code {
  font-family: inherit;
  background: transparent;
  padding: 0;
}

.code-expand-hint {
  text-align: center;
  padding: 0.5rem;
  background: #161b22;
  color: #58a6ff;
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.2s ease;
  border-top: 1px solid #30363d;
}

.code-expand-hint:hover {
  background: #21262d;
  color: #79c0ff;
}

/* Highlight.js 主题覆盖 - GitHub Dark 风格 */
.code-content :deep(.hljs-comment),
.code-content :deep(.hljs-quote) {
  color: #8b949e;
  font-style: italic;
}

.code-content :deep(.hljs-keyword),
.code-content :deep(.hljs-selector-tag) {
  color: #ff7b72;
}

.code-content :deep(.hljs-string),
.code-content :deep(.hljs-addition) {
  color: #a5d6ff;
}

.code-content :deep(.hljs-number),
.code-content :deep(.hljs-literal) {
  color: #79c0ff;
}

.code-content :deep(.hljs-built_in),
.code-content :deep(.hljs-builtin-name) {
  color: #ffa657;
}

.code-content :deep(.hljs-function),
.code-content :deep(.hljs-title) {
  color: #d2a8ff;
}

.code-content :deep(.hljs-class),
.code-content :deep(.hljs-type) {
  color: #ffa657;
}

.code-content :deep(.hljs-attr),
.code-content :deep(.hljs-attribute) {
  color: #79c0ff;
}

.code-content :deep(.hljs-variable),
.code-content :deep(.hljs-template-variable) {
  color: #ffa657;
}

.code-content :deep(.hljs-property) {
  color: #79c0ff;
}

.code-content :deep(.hljs-tag) {
  color: #7ee787;
}

.code-content :deep(.hljs-name) {
  color: #7ee787;
}

.code-content :deep(.hljs-selector-class),
.code-content :deep(.hljs-selector-id) {
  color: #d2a8ff;
}

.code-content :deep(.hljs-deletion) {
  color: #ffa198;
  background-color: rgba(248, 81, 73, 0.1);
}

.code-content :deep(.hljs-addition) {
  color: #7ee787;
  background-color: rgba(46, 160, 67, 0.1);
}

.code-content :deep(.hljs-emphasis) {
  font-style: italic;
}

.code-content :deep(.hljs-strong) {
  font-weight: bold;
}

/* 滚动条样式 */
.code-content::-webkit-scrollbar {
  height: 6px;
}

.code-content::-webkit-scrollbar-track {
  background: #161b22;
}

.code-content::-webkit-scrollbar-thumb {
  background: #30363d;
  border-radius: 3px;
}

.code-content::-webkit-scrollbar-thumb:hover {
  background: #484f58;
}
</style>

