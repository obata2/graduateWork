import axios from 'axios'

export const apiClient = axios.create({
  // eslint-disable-next-line no-undef
  baseURL: process.env.VUE_APP_API_BASE_URL
})