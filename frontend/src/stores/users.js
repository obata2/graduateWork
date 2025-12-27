// ユーザーについての情報を、どのコンポーネントからもアクセスできるようにする
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userId: 'admin' // 仮ID
  }),

  actions: {
    setUserId(id) {
      this.userId = id
    }
  }
})