const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    allowedHosts: 'all',
    client: {
      // ブラウザ側で実行されるWebSocketの接続先を、表示しているURLと同じにする設定
      webSocketURL: 'auto://0.0.0.0/ws',
    },
  }
})
