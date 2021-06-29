const express = require("express")
const router = express.Router()
const controller = require("../controller/notificationController")

router.post("/:token/notification/create", controller.create)
router.get("/:token/notification/get",controller.getNotificationsById)
router.put("/:token/notification/update/:id",controller.updateNotificationsById)
router.put("/:token/notification/update-all",controller.updateAllNotificationsById)
router.delete("/:token/notification/delete/:id",controller.deleteNotificationsById)
router.delete("/:token/notification/delete-all",controller.deleteAllNotificationsById)
module.exports = router