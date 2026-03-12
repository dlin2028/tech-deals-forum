import { writable } from 'svelte/store';

const defaultState = {
  deals: [],
  loading: false,
  error: null,
  filters: {
    q: '',
    category: [],
    minPrice: '',
    maxPrice: '',
    retailer: [],
    cpuBrand: [],
    gpuBrand: [],
    minBenchmark: '',
    sortBy: 'hot_score'
  },
  pagination: {
    page: 1,
    limit: 20,
    total: 0,
    hasMore: false
  }
};

export const dealsStore = writable({ ...defaultState });

export function resetDeals() {
  dealsStore.set({ ...defaultState });
}
