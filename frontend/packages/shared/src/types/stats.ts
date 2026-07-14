export interface CategoryStat { name: string; albumCount: number; episodeCount: number }
export interface ListeningStats { categoryCount: number; albumCount: number; episodeCount: number; categories: CategoryStat[] }
export interface IdentityStats { total: number; adminCount: number; userCount: number }