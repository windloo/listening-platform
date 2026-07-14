import type { Sentence } from './listening'

export interface EpisodeIndexDTO {
  id: string
  nameChinese: string
  nameEnglish: string
  albumId: string
  /** 命中的句子（带时间戳），仅包含匹配关键字的句子 */
  sentences: Sentence[]
}
