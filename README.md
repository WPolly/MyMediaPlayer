# MyMediaPlayer
简易音视频播放器
## 主要技术点
* 视频模块封装安卓系统自带VideoView,(Vitamio提供基本一样的api调用,体积较大,未加入本项目),能播放本地视频和网络视频,提供基本的播放控制控件以及基本手势操作控制,实现屏幕亮度,音量,全屏切换,播放进度等的控制.
* 音频播放模块没有使用aidl进行进程间交互,只使用Messenger,Handler实现Activity与Service的交互.通知栏显示与控制,自定义布局实现歌词的同步显示,可以解析标准的lrc歌词文件,高亮歌词显示过渡平缓流畅(需要将同名歌词文件放到与音乐文件相同的文件夹下).
* 还有许多有待于进一步完善的地方.

[本程序apk, 提取密码:w401][1]

[1]: http://pan.baidu.com/s/1geUXB8Z