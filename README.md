# WeChat_Hongbao
practice project to use AccessibilityNodeInfo and its service.

####在微信6.5.4上进行测试，练习掌握AccessibilityService的一些应用
<br>
**总结微信的特性，在2017年对红包插件已经有了较好的防护，具体如下：**<br>
1.红包的UI更改了多次，如“拆红包”等，“看看其他人的手气”均不在了<br>
2.ReceiveUI类（6.5.7中更为一串乱码）中的Button，过去“開”字是其text，目前整个控件用图片填充了<br>
3.每次更新微信会重置大部分的控件id，因此findAccessibiilityNodeInfoByViewId(String text)需要适配版本
<br><br>
**这个小练习的设计思路如下**<br>
1. 分为两个模式<br>
1.1. 收割模式（不停地点击最新红包）<br>
1.2. 列表模式（将屏幕上的所有红包倒序领走，重复红包跳过，然后回到桌面等待新红包)<br>
<br>
2. 分TYPE_NOTIFICATION_STATE_CHANGED和default两个分支<br>
2.1. 不在微信窗口中，从通知栏中收到带有“[微信红包]”字样的消息时，通过此通知进入微信窗口<br>
2.2. 已经在微信窗口中，监听所有的event，以触发event的控件类名来控制流程<br>
2.2.1. LauncherUI，启动了微信。需要获取所有红包；若flagOnGet标志位为*false*，则先领取表中剩余红包。寻找父节点到可点击的，点击<br>
2.2.2. LuckyMoneyReceiveUI，进入了红包界面<br>
2.2.2.1. 有按钮，这个红包还可以领取<br>
2.2.2.2. 没有按钮，这个红包已经无法领取，回退<br>
2.2.3. LuckyMoneyDetailUI，红包详情界面，回退<br>
<br><br>
由于红包详情界面回到聊天窗口时会触发微信LauncherUI里的方法，又可以获取屏幕上的红包了。<br>
####不过，想要获取窗口中的全部红包，就需要设置flagOnGet为false，直到列表的节点全部领取了，置为true，等新红包
