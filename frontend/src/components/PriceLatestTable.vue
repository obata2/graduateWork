<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  nameAndId: {
    type: Object,
    required: true
  },
  priceTransition: {
    type: Object,
    required: true
  },
  priceUnitList: {
    type: Object,
    required: true
  }
})

console.log(props)
//直近の価格を取得
const getPriceLatest = id => {
  const list = props.priceTransition[id]
  return Array.isArray(list) && list.length > 0 ? list[0] : "-"
}

//統計単位(グラム数とか)を取得
const getPriceUnitQty = id => props.priceUnitList[id] ?? "-"

</script>

<template>
  <table class="border-collapse border border-gray-400 w-full text-sm">
    <thead>
      <tr class="bg-gray-200 text-left">
        <th class="border border-gray-400 px-3 py-1">食材名</th>
        <th class="border border-gray-400 px-3 py-1">最新価格</th>
        <th class="border border-gray-400 px-3 py-1">統計単位</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(id, name) in props.nameAndId" :key="id">
        <td class="border border-gray-400 bg-white px-3 py-1">{{ name }}</td>
        <td class="border border-gray-400 bg-white px-3 py-1">{{ getPriceLatest(id) }} 円</td>
        <td class="border border-gray-400 bg-white px-3 py-1">{{ getPriceUnitQty(id) }}</td>
      </tr>
    </tbody>
  </table>
</template>