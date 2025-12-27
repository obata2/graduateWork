<script setup>
import { defineProps, onMounted, computed } from 'vue'


const props = defineProps({
  data: Object,
  isEditMode: Boolean
})

// 表の列名
const columns = [ "固定", "食材名", "価格", "単位"]

// 編集モードのときのみ点滅させる
const blinkBg = computed(() =>
  props.isEditMode ? "blink-bg" : ""
)

// 入力値が「1以上の整数値」であるかの判定
const isInvalidPrice = (value) => {
  return !Number.isInteger(value) || value < 1
}

// 編集前の価格情報を保持しておく
const prevPriceMap = new Map()
const onPriceFocus = (row) => {
  prevPriceMap.set(row, row.priceLatest)
}

// 値の書き換えが行われ、それが正常な値であれば、isFixedをtrueに変える
const onPriceBlur = (row) => {
  const prev = prevPriceMap.get(row)
  if (!isInvalidPrice(row.priceLatest) && prev !== row.priceLatest) {
    row.isFixed = true
  }
  prevPriceMap.delete(row)
}

onMounted(() =>
  console.log(props)
)
</script>

<template>
  <div class="overflow-auto rounded-lg border-2">
    <table class="w-full">
      <thead class="bg-gray-100 border-b-2">
        <tr>
          <th v-for="col in columns" :key="col.key" class="px-2 py-3 text-left text-xs font-bold text-gray-600 whitespace-nowrap">
            {{ col }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(row, rowIndex) in props.data" :key="rowIndex" class="">
          <!-- 固定 -->
          <td class="p-2 text-center" :class="blinkBg">
            <input 
              type="checkbox"
              v-model="row.isFixed"
              class="accent-gray-600"
              :disabled="!isEditMode"/>
          </td>
          <!-- 食材名 -->
          <td class="p-2 ">
            <div class="text-sm font-semibold">{{ row.ingredientName }}</div>
          </td>
          <!-- 最新価格 -->
          <td class="p-2 whitespace-nowrap" :class="blinkBg">
            <div class="text-sm font-semibold">
              <span class="text-xs font-medium text-gray-400 mr-0.5">¥</span>
              <input
                type="number"
                v-model.number="row.priceLatest"
                :readonly="!isEditMode"
                inputmode="numeric"
                :title="isInvalidPrice(row.priceLatest)
                  ? '1以上の整数値を入力してください'
                  : ''"
                @focus="onPriceFocus(row)"
                @blur="onPriceBlur(row)"
                :class="[
                  'w-12 text-sm no-spin',
                  isEditMode
                    ? isInvalidPrice(row.priceLatest)
                      ? 'border-2 border-red-500 rounded px-1 bg-white focus:outline-none'
                      : 'border rounded px-1 bg-white focus:outline-none focus:ring-1 focus:ring-green-500'
                    : 'border-0 bg-transparent pointer-events-none'
                ]"
              />
            </div>
          </td>
          <!-- 統計単位 -->
          <td class="p-2 whitespace-nowrap">
            <span class="text-sm font-bold inline-flex items-center px-1.5 py-0.5 rounded bg-gray-100 text-gray-500">
              {{ row.priceUnitQty }}
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>