<script setup>
import { ref } from 'vue'
import axios from 'axios'

// レスポンスを格納する変数（リアクティブ）
const message = ref('')
const calcResult = ref(null)

const callApi = async () => {
  try {
    const response = await axios.get('http://localhost:50000/api/hello')
    message.value = response.data   // ← レスポンスを変数messageに保存
  } catch (error) {
    console.error(error)
  }
}

const sendCalc = async () => {
  try {
    const response = await axios.post('http://localhost:50000/api/calc',
    { x: 5 },
    { headers: { 'Content-Type': 'application/json' } })
    calcResult.value = response.data   // ← 結果を変数calcResultに保存
  } catch (error) {
    console.error(error)
  }
}
</script>

<template>
  <div class="bg-secondary flex-1">
  <button @click="callApi">Hello API</button>
  <p v-if="message">{{ message }}</p>  <!-- ← ボタン下に表示 -->
  <br>
  <button @click="sendCalc">Calc API</button>
  <p v-if="calcResult !== null">計算結果: {{ calcResult }}</p>
  </div>
</template>