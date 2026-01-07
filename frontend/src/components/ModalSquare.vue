<script>
export default {
  name: "ModalSquare",
  props: {
    show: { type: Boolean, required: true },
    width: { type: String },   // 横幅
    height: { type: String },  // 高さ
    position: {
      type: String,
      default: "center", // "center" | "bottom"
    },
    closable: {
      type: Boolean,
      default: true
    },
    animation: {
      type: Boolean,
      default: false,
    }
  },
  emits: ["close"],
  methods: {
    close() {
      if (!this.closable) return
      this.$emit("close");
    },
  },
};
</script>

<template>
  <div v-if="show" class="fixed inset-0 flex justify-center bg-black bg-opacity-50 z-50"
    :class="position === 'bottom' ? 'items-end' : 'items-center'" @click.self="close">
    <transition :name="animation ? 'slide-up' : ''" appear>
      <div v-show="show" class="relative bg-white shadow-lg p-6"
        :class="position === 'bottom' ? 'rounded-t-lg rounded-b-none' : 'rounded-lg'"
        :style="{ width: width, height: height }">
        <!-- 閉じるボタン -->
        <button v-if="closable" class="absolute top-2 right-2 text-xl text-gray-400 hover:text-gray-600" @click="close">
          ✕
        </button>
        <!-- モーダルの中身をスロットで受け取る -->
        <slot></slot>
      </div>
    </transition>
  </div>
</template>