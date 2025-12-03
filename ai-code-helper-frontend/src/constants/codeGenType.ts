/**
 * 代码生成类型枚举
 */
export enum CodeGenTypeEnum {
  HTML = 'html',
  MULTI_FILE = 'multi_file',
  VUE_PROJECT = 'vue_project',
}

/**
 * 代码生成类型配置
 */
export const CODE_GEN_TYPE_CONFIG = {
  [CodeGenTypeEnum.HTML]: {
    label: '原生 HTML 模式',
    value: CodeGenTypeEnum.HTML,
    description: '单文件 HTML，适合简单页面',
  },
  [CodeGenTypeEnum.MULTI_FILE]: {
    label: '原生多文件模式',
    value: CodeGenTypeEnum.MULTI_FILE,
    description: '多文件原生项目，支持复杂结构',
  },
  [CodeGenTypeEnum.VUE_PROJECT]: {
    label: 'Vue 项目模式',
    value: CodeGenTypeEnum.VUE_PROJECT,
    description: 'Vue3 + Vite 前端工程化项目',
  },
}

/**
 * 代码生成类型选项列表
 */
export const CODE_GEN_TYPE_OPTIONS = Object.values(CODE_GEN_TYPE_CONFIG)

/**
 * 获取静态资源预览URL
 * @param codeGenType 代码生成类型
 * @param appId 应用ID
 * @returns 预览URL
 */
export const getStaticPreviewUrl = (codeGenType: string, appId: string | number): string => {
  const baseUrl = `/api/static/${codeGenType}_${appId}/`
  
  // 如果是 Vue 项目，浏览地址需要添加 dist 后缀
  if (codeGenType === CodeGenTypeEnum.VUE_PROJECT) {
    return `${baseUrl}dist/`
  }
  
  return baseUrl
}

/**
 * 获取代码生成类型的显示标签
 * @param codeGenType 代码生成类型
 * @returns 显示标签
 */
export const getCodeGenTypeLabel = (codeGenType: string): string => {
  // 兼容旧的 type 字段
  if (codeGenType === 'single') {
    return CODE_GEN_TYPE_CONFIG[CodeGenTypeEnum.HTML].label
  }
  if (codeGenType === 'multiple') {
    return CODE_GEN_TYPE_CONFIG[CodeGenTypeEnum.MULTI_FILE].label
  }
  
  return CODE_GEN_TYPE_CONFIG[codeGenType as CodeGenTypeEnum]?.label || codeGenType
}

