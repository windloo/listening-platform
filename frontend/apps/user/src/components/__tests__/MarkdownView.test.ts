import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import MarkdownView from '../MarkdownView.vue'

describe('MarkdownView', () => {
  it('renders bold, inline code, and table', () => {
    const w = mount(MarkdownView, { props: { content: '**bold** and `code`\n\n| a | b |\n|---|---|\n| 1 | 2 |' } })
    const html = w.html()
    expect(html).toContain('<strong>bold</strong>')
    expect(html).toContain('<code>code</code>')
    expect(html).toContain('<table>')
  })

  it('strips script tags (XSS)', () => {
    const w = mount(MarkdownView, { props: { content: '<script>alert(1)</script>hello' } })
    const html = w.html()
    expect(html).not.toContain('<script>')
    expect(html).toContain('hello')
  })

  it('opens links in new tab', () => {
    const w = mount(MarkdownView, { props: { content: '[b](http://x.com)' } })
    const html = w.html()
    expect(html).toContain('target="_blank"')
    expect(html).toContain('rel="noopener noreferrer"')
  })
})