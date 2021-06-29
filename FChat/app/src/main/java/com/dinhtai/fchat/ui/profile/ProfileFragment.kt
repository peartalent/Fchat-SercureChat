package com.dinhtai.fchat.ui.profile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.dinhtai.fchat.Begin
import com.dinhtai.fchat.R
import com.dinhtai.fchat.TestActivity
import com.dinhtai.fchat.base.BindingFragment
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.databinding.ActivityNotificationBinding
import com.dinhtai.fchat.databinding.FragmentProfileBinding
import com.dinhtai.fchat.ui.baseui.dialog.DetailMyInfoDialog
import com.dinhtai.fchat.ui.baseui.dialog.DialogBottomSheetShowImage
import com.dinhtai.fchat.ui.friend.FriendViewModel
import com.dinhtai.fchat.ui.main.ActivityMainViewModel
import com.dinhtai.fchat.ui.notification.NotificationActivity
import com.dinhtai.fchat.ui.notification.NotificationViewModel
import com.dinhtai.fchat.ui.profile.qrcode.QrCodeActivity
import com.dinhtai.fchat.ui.search.usernear.UserNearActivity
import com.dinhtai.fchat.utils.prepareFilePart
import com.google.firebase.auth.FirebaseAuth
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

class ProfileFragment : BindingFragment<FragmentProfileBinding>() {
    override fun getLayoutResId(): Int = R.layout.fragment_profile

    override val viewModel: ProfileViewModel
        get() = ViewModelProvider(this)[ProfileViewModel::class.java]
    private val viewModelMain: ActivityMainViewModel by lazy {
        ViewModelProvider(this)[ActivityMainViewModel::class.java]
    }
    private val viewModelNotificaion: NotificationViewModel by lazy {
        ViewModelProvider(this)[NotificationViewModel::class.java]
    }

    private val viewModelFriend: FriendViewModel by lazy {
        ViewModelProvider(this)[FriendViewModel::class.java]
    }
    private lateinit var dialogInfo: DetailMyInfoDialog
    private lateinit var controller: NavController
    override fun setupView() {
        binding.lifecycleOwner = viewLifecycleOwner
        InfoYourself.phone?.let { binding.phone = it }
        binding.vmMain = viewModelMain
        binding.vmNotification = viewModelNotificaion
        binding.vmFollow = viewModelFriend
        dialogInfo = DetailMyInfoDialog(viewLifecycleOwner,
            requireActivity(), viewModelMain, {
                requestPermionAndPickImage()
            })
        viewModelMain.getMyUser(InfoYourself.token!!)
        controller = findNavController()
        binding.linearNear.setOnClickListener {
            startActivity(Intent(context, UserNearActivity::class.java))
        }
        binding.buttonQrCode.setOnClickListener {
            startActivity(Intent(context, QrCodeActivity::class.java))
        }
        binding.imageAvatar.setOnClickListener { dialogInfo.show() }
        binding.buttonEdit.setOnClickListener { dialogInfo.show() }
        binding.linearNotice.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }
        binding.linearLogout.setOnClickListener {
            viewModelMain.logout(InfoYourself.token!!)
            var auth = FirebaseAuth.getInstance()
            auth.signOut()
            startActivity(Intent(context, Begin::class.java))
        }
        binding.buttonMore.setOnClickListener {
           showMenuPopupSetting(it)
        }
        nav()
    }

    override fun onResume() {
        super.onResume()
        viewModelNotificaion.getNotificationById(InfoYourself.token!!)
    }

    @SuppressLint("RestrictedApi")
    private fun showMenuPopupSetting(view: View) {
        val menuBuilder = MenuBuilder(requireContext())
        val inflater = MenuInflater(requireContext())
        inflater.inflate(R.menu.menu_setting_profile, menuBuilder)
        val optionsMenu = MenuPopupHelper(requireContext(), menuBuilder, view)
        optionsMenu.setForceShowIcon(true)
        menuBuilder.setCallback(object : MenuBuilder.Callback {
            override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                when (item!!.itemId) {
                    R.id.itemLogout -> {
                        viewModelMain.logout(InfoYourself.token!!)
                        var auth = FirebaseAuth.getInstance()
                        auth.signOut()
                        startActivity(Intent(context, Begin::class.java))
                        true
                    }
                    else -> false
                }
                return true
            }

            override fun onMenuModeChange(menu: MenuBuilder) {}
        })
        optionsMenu.show()
    }

    private fun nav() {
        binding.linearFriend.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToFriendFragment()
            controller.navigate(action)
        }
        binding.linearGroup.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToGroupFragment()
            controller.navigate(action)
        }
    }

    //Yêu cầu quyền truy câp
    private fun requestPermionAndPickImage() {
        val permissionlistener = object : PermissionListener {
            override fun onPermissionGranted() {
                startCropImageActivity()
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
            }
        }
        TedPermission.with(requireContext())
            .setPermissionListener(permissionlistener)
            .setDeniedMessage("Nếu bạn từ chối quyền, bạn không thể sử dụng dịch vụ này\n\n làm ơn hãy chấp nhận bằng cách [Setting] > [Permission]")
            .setPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .check();
    }

    private fun startCropImageActivity() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(4, 4)
            .start(requireContext(), this);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == AppCompatActivity.RESULT_OK) {
                val resultUri: Uri = result.uri

                //encodeImage = requireContext().encodeImage(resultUri)
                resultUri?.let {
                    viewModelMain.updateAvatarUser(
                        InfoYourself.token!!,
                        requireContext().prepareFilePart(it, "avatar")
                    )
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
            }
        }
    }
}
