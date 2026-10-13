import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import DOMPurify from 'dompurify'

const md = new MarkdownIt({
  html: false, // 禁止原始 HTML，避免注入
  linkify: true,
  breaks: true,
  highlight(code, language) {
    const escaped = md.utils.escapeHtml(code)
    if (language && hljs.getLanguage(language)) {
      try {
        const highlighted = hljs.highlight(code, { language, ignoreIllegals: true }).value
        return `<pre class="hljs"><code class="language-${language}">${highlighted}</code></pre>`
      } catch {
        // 高亮失败时退化为纯文本
      }
    }
    return `<pre class="hljs"><code>${escaped}</code></pre>`
  }
})

/**
 * 渲染 AI 输出的 Markdown，并经 DOMPurify 净化后返回安全 HTML。
 */
export function renderMarkdown(text) {
  if (!text) return ''
  return DOMPurify.sanitize(md.render(text))
}

/**
 * 提取纯文本（用于会话标题、复制等场景）。
 */
export function toPlainText(text, maxLength = 40) {
  if (!text) return ''
  const plain = text.replace(/[#*`>\-\[\]()]/g, ' ').replace(/\s+/g, ' ').trim()
  return plain.length > maxLength ? `${plain.slice(0, maxLength)}…` : plain
}
