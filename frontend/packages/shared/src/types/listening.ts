export interface BaseEntity { id: string; createTime: string; updateTime: string }

export interface Category extends BaseEntity {
  sequenceNumber: number; nameChinese: string; nameEnglish: string; coverUrl: string | null
  createdBy: string
}
export interface Album extends BaseEntity {
  sequenceNumber: number; nameChinese: string; nameEnglish: string; categoryId: string; isVisible: boolean
  createdBy: string
}
export interface Sentence { startMs: number; endMs: number; text: string }

export interface Episode extends BaseEntity {
  sequenceNumber: number; nameChinese: string; nameEnglish: string; albumId: string
  audioUrl: string; durationInSecond: number; subtitle: string | null; subtitleType: string; isVisible: boolean
  createdBy: string
  sentences: Sentence[] | null
}

export interface CategoryReq { nameChinese: string; nameEnglish: string; coverUrl?: string | null }
export interface AlbumReq { nameChinese: string; nameEnglish: string; categoryId: string }
export interface EpisodeReq {
  nameChinese: string; nameEnglish: string; albumId: string
  audioUrl: string; durationInSecond: number; subtitle: string; subtitleType: string
}