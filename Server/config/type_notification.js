module.exports = {
    NOTIFICATION_MESSAGE: "message",
    NOTIFICAtiON_CALL: "call",
    TITLE_MESSAGE: "Tin nhắn đến",
    TITLE_CALL_RING: "Cuộc gọi đến",
    TITLE_CALL_MISS: "Cuộc gọi nhỡ",
    TITLE_FOLLOW: "Chờ kết bạn",
    TITLE_ACCEPT_FOLLOW: "Đồng ý kết bạn",
    MESSAGE_VOICE: "đã gửi một hội thoại",
    MESSAGE_IMAGE: "đã gửi một hình ảnh",
    MESSAGE_FOLLOW: "đã gửi một lời mời kết bạn",
    MESSAGE_ACCEPT: "Giờ hai người đã là bạn",
    MESSAGE_CALL: "đang gọi bạn",
    noticeKey: (title, type, content, userId,phone) => {
        var payload = {
            data: {
                type: type,
                title: title,
                key: content,
                user_id: userId,
                phone:phone
            }
        };
        return payload;
    },
    notice: (title, type, fullname, content, chanelId, avatarUrl) => {
        var payload = {
            data: {
                type: type,
                title: title,
                body: `${fullname}: Đã gửi bạn một tin nhắn`,
                chanel_id: chanelId
            }
        };
        return payload;
    },
    noticeCall: (title, fullname, userId, avatar,content) => {
        console.log("content notice call",content)
        var payload = {
            data: {
                title: title,
                user_id: userId,
                avatar: avatar,
                fullname: fullname,
               // meeting_type:content.meeting_type,
                type: content.type
            }
        };
        if(content.meeting_type){
            payload.data.meeting_type=content.meeting_type
        }
        if(content.meeting_type){
            payload.data.meeting_room=content.meeting_room
        }
        if(content.invitationResponse){
            payload.data.invitationResponse=content.invitationResponse
        }
        return payload;
    }

};
// notification: {
//     title: title,
//         body: `${fullname}: ${content}`,
//         image: `http://192.168.100.164:3000/${avatarUrl}`
// },