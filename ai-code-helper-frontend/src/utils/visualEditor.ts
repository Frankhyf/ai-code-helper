/**
 * 可视化编辑工具
 * 用于在 iframe 中选择元素并与主页面通信
 */

// 选中元素的信息结构
export interface SelectedElementInfo {
  tagName: string           // 元素标签名
  id?: string               // 元素 ID
  className?: string        // 元素 class
  textContent?: string      // 元素文本内容（截断）
  xpath?: string            // 元素 XPath
  cssSelector?: string      // CSS 选择器
  attributes?: Record<string, string>  // 元素属性
  boundingRect?: {          // 元素位置信息
    top: number
    left: number
    width: number
    height: number
  }
}

// 消息类型定义
export interface VisualEditorMessage {
  type: 'VISUAL_EDITOR_ELEMENT_SELECTED' | 'VISUAL_EDITOR_ELEMENT_HOVER' | 'VISUAL_EDITOR_MODE_CHANGED'
  payload: SelectedElementInfo | null
}

// 编辑模式配置
export interface VisualEditorConfig {
  hoverBorderColor: string      // 悬浮边框颜色
  selectedBorderColor: string   // 选中边框颜色
  hoverBorderWidth: string      // 边框宽度
}

const DEFAULT_CONFIG: VisualEditorConfig = {
  hoverBorderColor: 'rgba(59, 130, 246, 0.8)',    // 蓝色半透明
  selectedBorderColor: 'rgba(239, 68, 68, 1)',    // 红色
  hoverBorderWidth: '2px',
}

/**
 * 生成元素的 CSS 选择器
 */
function generateCssSelector(element: Element): string {
  if (element.id) {
    return `#${element.id}`
  }
  
  const parts: string[] = []
  let current: Element | null = element
  
  while (current && current !== document.body && current !== document.documentElement) {
    let selector = current.tagName.toLowerCase()
    
    if (current.id) {
      selector = `#${current.id}`
      parts.unshift(selector)
      break
    }
    
    if (current.className && typeof current.className === 'string') {
      const classes = current.className.trim().split(/\s+/).filter(c => c).slice(0, 2)
      if (classes.length > 0) {
        selector += '.' + classes.join('.')
      }
    }
    
    // 添加 nth-child 以提高唯一性
    const parent = current.parentElement
    if (parent) {
      const siblings = Array.from(parent.children).filter(
        child => child.tagName === current!.tagName
      )
      if (siblings.length > 1) {
        const index = siblings.indexOf(current) + 1
        selector += `:nth-child(${index})`
      }
    }
    
    parts.unshift(selector)
    current = current.parentElement
  }
  
  return parts.join(' > ')
}

/**
 * 生成元素的 XPath
 */
function generateXPath(element: Element): string {
  if (element.id) {
    return `//*[@id="${element.id}"]`
  }
  
  const parts: string[] = []
  let current: Element | null = element
  
  while (current && current.nodeType === Node.ELEMENT_NODE) {
    let index = 1
    let sibling: Element | null = current.previousElementSibling
    
    while (sibling) {
      if (sibling.tagName === current.tagName) {
        index++
      }
      sibling = sibling.previousElementSibling
    }
    
    const tagName = current.tagName.toLowerCase()
    const part = index > 1 ? `${tagName}[${index}]` : tagName
    parts.unshift(part)
    
    current = current.parentElement
  }
  
  return '/' + parts.join('/')
}

/**
 * 提取元素信息
 */
function extractElementInfo(element: Element): SelectedElementInfo {
  const rect = element.getBoundingClientRect()
  const textContent = element.textContent?.trim() || ''
  
  // 提取关键属性
  const attributes: Record<string, string> = {}
  const importantAttrs = ['src', 'href', 'alt', 'title', 'placeholder', 'type', 'name', 'value']
  importantAttrs.forEach(attr => {
    const value = element.getAttribute(attr)
    if (value) {
      attributes[attr] = value
    }
  })
  
  return {
    tagName: element.tagName.toLowerCase(),
    id: element.id || undefined,
    className: element.className && typeof element.className === 'string' 
      ? element.className.trim() || undefined 
      : undefined,
    textContent: textContent.length > 100 ? textContent.slice(0, 100) + '...' : textContent,
    xpath: generateXPath(element),
    cssSelector: generateCssSelector(element),
    attributes: Object.keys(attributes).length > 0 ? attributes : undefined,
    boundingRect: {
      top: rect.top,
      left: rect.left,
      width: rect.width,
      height: rect.height,
    },
  }
}

/**
 * 生成注入到 iframe 中的编辑脚本
 */
export function generateEditorScript(config: VisualEditorConfig = DEFAULT_CONFIG): string {
  return `
(function() {
  // 防止重复注入
  if (window.__VISUAL_EDITOR_INJECTED__) {
    console.log('[VisualEditor] 已注入，跳过');
    return;
  }
  window.__VISUAL_EDITOR_INJECTED__ = true;
  
  console.log('[VisualEditor] 编辑模式已启用');
  
  const config = ${JSON.stringify(config)};
  
  // 创建高亮覆盖层
  let hoverOverlay = null;
  let selectedOverlay = null;
  let selectedElement = null;
  
  function createOverlay(color) {
    const overlay = document.createElement('div');
    overlay.style.cssText = \`
      position: fixed;
      pointer-events: none;
      border: ${config.hoverBorderWidth} solid \${color};
      background: \${color.replace(')', ', 0.1)').replace('rgba', 'rgba').replace('rgb', 'rgba')};
      z-index: 999999;
      transition: all 0.1s ease;
      box-sizing: border-box;
    \`;
    document.body.appendChild(overlay);
    return overlay;
  }
  
  function updateOverlay(overlay, element) {
    if (!element || !overlay) return;
    const rect = element.getBoundingClientRect();
    overlay.style.top = rect.top + 'px';
    overlay.style.left = rect.left + 'px';
    overlay.style.width = rect.width + 'px';
    overlay.style.height = rect.height + 'px';
    overlay.style.display = 'block';
  }
  
  function hideOverlay(overlay) {
    if (overlay) {
      overlay.style.display = 'none';
    }
  }
  
  // 生成 CSS 选择器
  function generateCssSelector(element) {
    if (element.id) {
      return '#' + element.id;
    }
    
    const parts = [];
    let current = element;
    
    while (current && current !== document.body && current !== document.documentElement) {
      let selector = current.tagName.toLowerCase();
      
      if (current.id) {
        selector = '#' + current.id;
        parts.unshift(selector);
        break;
      }
      
      if (current.className && typeof current.className === 'string') {
        const classes = current.className.trim().split(/\\s+/).filter(c => c).slice(0, 2);
        if (classes.length > 0) {
          selector += '.' + classes.join('.');
        }
      }
      
      const parent = current.parentElement;
      if (parent) {
        const siblings = Array.from(parent.children).filter(
          child => child.tagName === current.tagName
        );
        if (siblings.length > 1) {
          const index = siblings.indexOf(current) + 1;
          selector += ':nth-child(' + index + ')';
        }
      }
      
      parts.unshift(selector);
      current = current.parentElement;
    }
    
    return parts.join(' > ');
  }
  
  // 生成 XPath
  function generateXPath(element) {
    if (element.id) {
      return '//*[@id="' + element.id + '"]';
    }
    
    const parts = [];
    let current = element;
    
    while (current && current.nodeType === Node.ELEMENT_NODE) {
      let index = 1;
      let sibling = current.previousElementSibling;
      
      while (sibling) {
        if (sibling.tagName === current.tagName) {
          index++;
        }
        sibling = sibling.previousElementSibling;
      }
      
      const tagName = current.tagName.toLowerCase();
      const part = index > 1 ? tagName + '[' + index + ']' : tagName;
      parts.unshift(part);
      
      current = current.parentElement;
    }
    
    return '/' + parts.join('/');
  }
  
  // 提取元素信息
  function extractElementInfo(element) {
    const rect = element.getBoundingClientRect();
    const textContent = (element.textContent || '').trim();
    
    const attributes = {};
    const importantAttrs = ['src', 'href', 'alt', 'title', 'placeholder', 'type', 'name', 'value'];
    importantAttrs.forEach(attr => {
      const value = element.getAttribute(attr);
      if (value) {
        attributes[attr] = value;
      }
    });
    
    return {
      tagName: element.tagName.toLowerCase(),
      id: element.id || undefined,
      className: element.className && typeof element.className === 'string' 
        ? element.className.trim() || undefined 
        : undefined,
      textContent: textContent.length > 100 ? textContent.slice(0, 100) + '...' : textContent,
      xpath: generateXPath(element),
      cssSelector: generateCssSelector(element),
      attributes: Object.keys(attributes).length > 0 ? attributes : undefined,
      boundingRect: {
        top: rect.top,
        left: rect.left,
        width: rect.width,
        height: rect.height,
      },
    };
  }
  
  // 发送消息到父窗口
  function sendMessage(type, payload) {
    window.parent.postMessage({
      type: type,
      payload: payload
    }, '*');
  }
  
  // 初始化覆盖层
  hoverOverlay = createOverlay(config.hoverBorderColor);
  selectedOverlay = createOverlay(config.selectedBorderColor);
  hideOverlay(hoverOverlay);
  hideOverlay(selectedOverlay);
  
  // 鼠标移动事件
  document.addEventListener('mousemove', function(e) {
    const target = e.target;
    if (target === hoverOverlay || target === selectedOverlay) return;
    if (target === document.body || target === document.documentElement) {
      hideOverlay(hoverOverlay);
      return;
    }
    
    updateOverlay(hoverOverlay, target);
  }, true);
  
  // 鼠标离开文档
  document.addEventListener('mouseleave', function() {
    hideOverlay(hoverOverlay);
  }, true);
  
  // 点击事件
  document.addEventListener('click', function(e) {
    const target = e.target;
    if (target === hoverOverlay || target === selectedOverlay) return;
    if (target === document.body || target === document.documentElement) return;
    
    e.preventDefault();
    e.stopPropagation();
    
    selectedElement = target;
    updateOverlay(selectedOverlay, target);
    
    const info = extractElementInfo(target);
    console.log('[VisualEditor] 选中元素:', info);
    sendMessage('VISUAL_EDITOR_ELEMENT_SELECTED', info);
  }, true);
  
  // 监听来自父窗口的消息（用于退出编辑模式）
  window.addEventListener('message', function(e) {
    if (e.data && e.data.type === 'VISUAL_EDITOR_EXIT') {
      console.log('[VisualEditor] 退出编辑模式');
      if (hoverOverlay) hoverOverlay.remove();
      if (selectedOverlay) selectedOverlay.remove();
      window.__VISUAL_EDITOR_INJECTED__ = false;
    }
    
    if (e.data && e.data.type === 'VISUAL_EDITOR_CLEAR_SELECTION') {
      console.log('[VisualEditor] 清除选中');
      selectedElement = null;
      hideOverlay(selectedOverlay);
    }
  });
  
  console.log('[VisualEditor] 初始化完成');
})();
`
}

/**
 * 将编辑脚本注入到 iframe 中
 */
export function injectEditorScript(iframe: HTMLIFrameElement, config?: VisualEditorConfig): boolean {
  try {
    const iframeWindow = iframe.contentWindow
    const iframeDocument = iframe.contentDocument
    
    if (!iframeWindow || !iframeDocument) {
      console.error('[VisualEditor] 无法访问 iframe 内容')
      return false
    }
    
    // 创建并注入脚本
    const script = iframeDocument.createElement('script')
    script.textContent = generateEditorScript(config)
    iframeDocument.body.appendChild(script)
    
    console.log('[VisualEditor] 脚本注入成功')
    return true
  } catch (error) {
    console.error('[VisualEditor] 注入脚本失败:', error)
    return false
  }
}

/**
 * 退出编辑模式（通知 iframe）
 */
export function exitEditorMode(iframe: HTMLIFrameElement): void {
  try {
    iframe.contentWindow?.postMessage({
      type: 'VISUAL_EDITOR_EXIT'
    }, '*')
    console.log('[VisualEditor] 已发送退出编辑模式消息')
  } catch (error) {
    console.error('[VisualEditor] 发送退出消息失败:', error)
  }
}

/**
 * 清除选中状态（通知 iframe）
 */
export function clearSelection(iframe: HTMLIFrameElement): void {
  try {
    iframe.contentWindow?.postMessage({
      type: 'VISUAL_EDITOR_CLEAR_SELECTION'
    }, '*')
    console.log('[VisualEditor] 已发送清除选中消息')
  } catch (error) {
    console.error('[VisualEditor] 发送清除选中消息失败:', error)
  }
}

/**
 * 格式化选中元素信息为提示词
 */
export function formatElementInfoForPrompt(element: SelectedElementInfo): string {
  const parts: string[] = []
  
  parts.push(`【选中元素信息】`)
  parts.push(`- 标签: <${element.tagName}>`)
  
  if (element.id) {
    parts.push(`- ID: ${element.id}`)
  }
  
  if (element.className) {
    parts.push(`- Class: ${element.className}`)
  }
  
  if (element.cssSelector) {
    parts.push(`- CSS选择器: ${element.cssSelector}`)
  }
  
  if (element.textContent) {
    parts.push(`- 文本内容: "${element.textContent}"`)
  }
  
  if (element.attributes && Object.keys(element.attributes).length > 0) {
    const attrStr = Object.entries(element.attributes)
      .map(([k, v]) => `${k}="${v}"`)
      .join(', ')
    parts.push(`- 属性: ${attrStr}`)
  }
  
  return parts.join('\n')
}

/**
 * 创建消息监听器
 */
export function createMessageListener(
  onElementSelected: (info: SelectedElementInfo) => void
): () => void {
  const handler = (event: MessageEvent) => {
    if (event.data && event.data.type === 'VISUAL_EDITOR_ELEMENT_SELECTED') {
      const info = event.data.payload as SelectedElementInfo
      if (info) {
        onElementSelected(info)
      }
    }
  }
  
  window.addEventListener('message', handler)
  
  // 返回清理函数
  return () => {
    window.removeEventListener('message', handler)
  }
}

