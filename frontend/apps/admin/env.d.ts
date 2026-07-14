/// <reference types="vite/client" />
interface ImportMetaEnv { readonly VITE_API_BASE: string }
interface ImportMeta { readonly env: ImportMetaEnv }
declare module '*.vue' { import type { DefineComponent } from 'vue'; const c: DefineComponent<{}, {}, any>; export default c }
