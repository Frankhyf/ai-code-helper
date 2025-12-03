<template>
  <div class="min-h-screen pt-6 pb-8">
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
      <!-- 页面标题 -->
      <div class="text-center mb-12">
        <h1 class="text-4xl font-bold gradient-text mb-4">关于我们</h1>
        <p class="text-xl text-gray-300">
          让AI技术赋能每个人，轻松创建专业网站应用
        </p>
      </div>

      <!-- 主要内容 -->
      <div class="space-y-12">
        <!-- 项目介绍 -->
        <section class="glass-dark rounded-2xl p-8">
          <h2 class="text-2xl font-bold text-white mb-6">项目愿景</h2>
          <div class="text-gray-300 space-y-4">
            <p>
              AI应用生成平台是一个革命性的工具，旨在让每个人都能轻松创建专业的网站应用。
              无论您是开发者、设计师、企业家还是普通用户，只需用自然语言描述您的需求，
              我们的AI就能为您生成完整的网站应用。
            </p>
            <p>
              我们相信技术的力量应该为每个人所用，而不是局限于专业的开发人员。
              通过AI技术，我们正在打破技术壁垒，让创意和想法能够快速转化为现实。
            </p>
          </div>
        </section>

        <!-- 核心特性 -->
        <section class="glass-dark rounded-2xl p-8">
          <h2 class="text-2xl font-bold text-white mb-6">核心特性</h2>
          <div class="grid md:grid-cols-2 gap-6">
            <div class="flex items-start space-x-4">
              <div class="w-12 h-12 bg-gradient-to-r from-blue-500 to-cyan-500 rounded-lg flex items-center justify-center flex-shrink-0">
                <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"></path>
                </svg>
              </div>
              <div>
                <h3 class="text-lg font-semibold text-white mb-2">智能对话</h3>
                <p class="text-gray-400">
                  通过自然语言与AI对话，描述您的需求，AI理解并生成相应的应用代码
                </p>
              </div>
            </div>
            
            <div class="flex items-start space-x-4">
              <div class="w-12 h-12 bg-gradient-to-r from-purple-500 to-pink-500 rounded-lg flex items-center justify-center flex-shrink-0">
                <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                </svg>
              </div>
              <div>
                <h3 class="text-lg font-semibold text-white mb-2">实时预览</h3>
                <p class="text-gray-400">
                  AI生成过程中实时预览效果，所见即所得，支持多轮对话优化
                </p>
              </div>
            </div>
            
            <div class="flex items-start space-x-4">
              <div class="w-12 h-12 bg-gradient-to-r from-green-500 to-teal-500 rounded-lg flex items-center justify-center flex-shrink-0">
                <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"></path>
                </svg>
              </div>
              <div>
                <h3 class="text-lg font-semibold text-white mb-2">一键部署</h3>
                <p class="text-gray-400">
                  生成完成后一键部署到云端，获得永久访问链接，支持自定义域名
                </p>
              </div>
            </div>
            
            <div class="flex items-start space-x-4">
              <div class="w-12 h-12 bg-gradient-to-r from-orange-500 to-red-500 rounded-lg flex items-center justify-center flex-shrink-0">
                <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
                </svg>
              </div>
              <div>
                <h3 class="text-lg font-semibold text-white mb-2">应用管理</h3>
                <p class="text-gray-400">
                  完整的应用生命周期管理，支持编辑、部署、删除等操作
                </p>
              </div>
            </div>
          </div>
        </section>

        <!-- 技术架构 -->
        <section class="glass-dark rounded-2xl p-8">
          <h2 class="text-2xl font-bold text-white mb-6">技术架构</h2>
          <div class="grid md:grid-cols-2 gap-8">
            <div>
              <h3 class="text-lg font-semibold text-white mb-4">前端技术栈</h3>
              <ul class="text-gray-400 space-y-2">
                <li>• Vue 3 (Composition API) - 现代化前端框架</li>
                <li>• TypeScript - 类型安全的JavaScript</li>
                <li>• Vite - 极速构建工具</li>
                <li>• Tailwind CSS - 实用优先的CSS框架</li>
                <li>• Pinia - 状态管理</li>
                <li>• Vue Router 4 - 路由管理</li>
              </ul>
            </div>
            
            <div>
              <h3 class="text-lg font-semibold text-white mb-4">后端技术栈</h3>
              <ul class="text-gray-400 space-y-2">
                <li>• Spring Boot 3.5.4 - 现代化Java框架</li>
                <li>• Spring MVC - Web框架</li>
                <li>• MyBatis-Flex - ORM框架</li>
                <li>• MySQL - 关系型数据库</li>
                <li>• Redis - 缓存数据库</li>
                <li>• LangChain4j - AI集成框架</li>
              </ul>
            </div>
          </div>
        </section>

        <!-- 联系我们 -->
        <section class="glass-dark rounded-2xl p-8">
          <h2 class="text-2xl font-bold text-white mb-6">联系我们</h2>
          <div class="text-gray-300 space-y-4">
            <p>
              如果您在使用过程中遇到任何问题，或者有任何建议和意见，
              欢迎通过以下方式联系我们：
            </p>
            <div class="flex flex-col sm:flex-row gap-4">
              <a 
                href="mailto:support@ai-app-generator.com"
                class="flex items-center space-x-2 bg-blue-500/20 hover:bg-blue-500/30 text-blue-400 px-4 py-2 rounded-lg transition-colors"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
                </svg>
                <span>发送邮件</span>
              </a>
              
              <a 
                href="https://github.com/ai-app-generator"
                target="_blank"
                class="flex items-center space-x-2 bg-gray-500/20 hover:bg-gray-500/30 text-gray-400 px-4 py-2 rounded-lg transition-colors"
              >
                <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
                </svg>
                <span>GitHub</span>
              </a>
            </div>
          </div>
        </section>

        <!-- 版本信息 -->
        <section class="text-center">
          <div class="glass-dark rounded-2xl p-6">
            <p class="text-gray-400">
              © 2024 AI应用生成平台. 版本 v1.0.0
            </p>
            <p class="text-sm text-gray-500 mt-2">
              让创意无限可能，让技术触手可及
            </p>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>