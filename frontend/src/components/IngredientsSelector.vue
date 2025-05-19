<!-- frontend/src/components/IngredientSelector.vue -->
<template>
  <div>
    <h2>食材を選択してください：</h2>
    <div v-for="item in ingredients" :key="item">
      <label>
        <input type="checkbox" :value="item" v-model="selected" />
        {{ item }}
      </label>
    </div>
    <button @click="submitIngredients">送信</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const ingredients = ['米', '豚肉', '牛肉', '玉ねぎ']
const selected = ref([])

const submitIngredients = async () => {
  try {
    const response = await axios.post('http://localhost:8080/api/ingredients', selected.value)
    console.log('Springからの応答:', response.data)
  } catch (error) {
    console.error('通信エラー:', error)
  }
}
</script>
