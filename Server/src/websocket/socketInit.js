const clientConn = require('./clientConnection')
const chatText = require('./chat/chatText')
const chatImage = require('./chat/chatImage')
const chatVoice = require('./chat/chatVoice')
const chatVideoCall = require('./chat/chatVideoCall')
const deleteMessage = require('./deleteMessage')
module.exports = {
    initSocketLogin: (ws) => {
        clientConn.loginClient(ws)
    },
    initSocketChat: (ws) => {
        chatText.chatTextOneClient(ws)
        chatImage.chatImageOneClient(ws)
        chatVoice.chatVoiceOneClient(ws)
        deleteMessage.deleteMessageToClient(ws)
    },
    initSocketCall: (ws) => {
        chatVideoCall.chatVideoCallOneClient(ws)
    }
}