import p5 from 'p5'

export const initHeroBackground = (containerId: string) => {
  const container = document.getElementById(containerId)
  if (!container) return null

  // 清空容器中的旧内容，防止多个 canvas 堆叠
  container.innerHTML = ''

  const sketch = (p: any) => {
    let particles: any[] = []
    let connections: any[] = []
    const numParticles = 80
    const connectionDistance = 120

    p.setup = () => {
      const canvas = p.createCanvas(container.offsetWidth, container.offsetHeight)
      canvas.parent(containerId)
      
      // 初始化粒子
      for (let i = 0; i < numParticles; i++) {
        particles.push({
          x: p.random(p.width),
          y: p.random(p.height),
          vx: p.random(-0.5, 0.5),
          vy: p.random(-0.5, 0.5),
          size: p.random(2, 4),
          opacity: p.random(0.3, 0.8)
        })
      }
    }

    p.draw = () => {
      // 渐变背景
      for (let i = 0; i <= p.height; i++) {
        const inter = p.map(i, 0, p.height, 0, 1)
        const c = p.lerpColor(
          p.color(15, 23, 42, 100), // slate-900
          p.color(30, 41, 59, 100), // slate-800
          inter
        )
        p.stroke(c)
        p.line(0, i, p.width, i)
      }

      // 更新和绘制粒子
      particles.forEach((particle, i) => {
        // 更新位置
        particle.x += particle.vx
        particle.y += particle.vy

        // 边界检测
        if (particle.x < 0 || particle.x > p.width) particle.vx *= -1
        if (particle.y < 0 || particle.y > p.height) particle.vy *= -1

        // 绘制粒子
        p.fill(59, 130, 246, particle.opacity * 255)
        p.noStroke()
        p.circle(particle.x, particle.y, particle.size)

        // 鼠标交互
        if (p.mouseX && p.mouseY) {
          const mouseDistance = p.dist(particle.x, particle.y, p.mouseX, p.mouseY)
          if (mouseDistance < 100) {
            const force = p.map(mouseDistance, 0, 100, 0.02, 0)
            const angle = p.atan2(particle.y - p.mouseY, particle.x - p.mouseX)
            particle.vx += p.cos(angle) * force
            particle.vy += p.sin(angle) * force
          }
        }

        // 粒子间连线
        for (let j = i + 1; j < particles.length; j++) {
          const other = particles[j]
          const distance = p.dist(particle.x, particle.y, other.x, other.y)
          
          if (distance < connectionDistance) {
            const alpha = p.map(distance, 0, connectionDistance, 0.3, 0)
            p.stroke(59, 130, 246, alpha * 255)
            p.strokeWeight(1)
            p.line(particle.x, particle.y, other.x, other.y)
          }
        }

        // 速度限制
        particle.vx = p.constrain(particle.vx, -1, 1)
        particle.vy = p.constrain(particle.vy, -1, 1)
      })

      // 添加浮动的几何图形
      p.push()
      p.translate(p.width / 2, p.height / 2)
      p.rotate(p.frameCount * 0.005)
      p.noFill()
      p.stroke(6, 182, 212, 50) // cyan-500 with low opacity
      p.strokeWeight(1)
      p.rect(-100, -100, 200, 200)
      p.pop()

      p.push()
      p.translate(p.width * 0.7, p.height * 0.3)
      p.rotate(-p.frameCount * 0.003)
      p.noFill()
      p.stroke(139, 92, 246, 50) // violet-500 with low opacity
      p.strokeWeight(1)
      p.triangle(0, -50, -43, 25, 43, 25)
      p.pop()
    }

    p.windowResized = () => {
      p.resizeCanvas(container.offsetWidth, container.offsetHeight)
    }

    p.mouseMoved = () => {
      // 鼠标移动时的交互效果
      particles.forEach(particle => {
        const distance = p.dist(particle.x, particle.y, p.mouseX, p.mouseY)
        if (distance < 150) {
          const force = p.map(distance, 0, 150, 0.05, 0)
          const angle = p.atan2(particle.y - p.mouseY, particle.x - p.mouseX)
          particle.vx += p.cos(angle) * force
          particle.vy += p.sin(angle) * force
        }
      })
    }
  }

  // 返回 p5 实例，以便在组件卸载时清理
  return new p5(sketch)
}