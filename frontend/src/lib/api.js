import axios from 'axios';

const instance = axios.create({ baseURL: '/api' });

instance.interceptors.request.use(config => {
  if (typeof localStorage !== 'undefined') {
    const token = localStorage.getItem('token');
    if (token) config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
});

export const api = {
  auth: {
    login: (email, password) => instance.post('/auth/login', { email, password }),
    register: (username, email, password) => instance.post('/auth/register', { username, email, password }),
    me: () => instance.get('/auth/me')
  },
  deals: {
    list: (params) => instance.get('/deals', { params }),
    search: (filters) => instance.get('/deals/search', { params: filters }),
    hot: () => instance.get('/deals/hot'),
    feed: () => instance.get('/deals/feed'),
    get: (id) => instance.get(`/deals/${id}`),
    create: (data) => instance.post('/deals', data),
    vote: (id, voteType) => instance.post(`/deals/${id}/vote`, { vote_type: voteType }),
    removeVote: (id) => instance.delete(`/deals/${id}/vote`),
    priceHistory: (id) => instance.get(`/deals/${id}/price-history`)
  },
  components: {
    search: (q) => instance.get('/components/search', { params: { q } }),
    create: (data) => instance.post('/components', data)
  }
};
