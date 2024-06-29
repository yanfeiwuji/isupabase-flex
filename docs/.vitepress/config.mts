import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "Isupabase Flex",
  description: "Isupabase Flex",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Examples', link: '/markdown-examples' }
    ],

    sidebar: [
      {
        text: 'Brief',
        items: [
          { text: 'Quick Start', link: '/quickstart' },
          { text: 'Something Different', link: '/different' },
          { text: 'Customize Your Provider', link: '/provider' }
        ]
      }
    ],
    search:{
      provider:"local",
    },
    socialLinks: [
      { icon: 'github', link: 'https://github.com/vuejs/vitepress' }
    ]
  }
})
