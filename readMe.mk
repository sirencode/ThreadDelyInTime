##设计思路：
   每个APP都有一个过渡页，或者叫做广告页，一般的处理方法就是直接睡一秒(具体时间不定)或者通过发送一个delay的handler。而这个时间是算到APP的启动时间里面的，而APP的启动时间又是衡量一个APP是否优秀的重要指标。因此，现在就变成了这种情况，APP的 启动时间=各种初始化时间+必要逻辑处理时间+广告页显示时间。

   这一切看起来并没有什么不对的，但是我有一段时间再做APP的性能优化，我知道优化个几十毫秒都很费力的（我把项目里面所有的注解
全部换掉才减少了十几毫秒，事件总线这个去掉减少了200多毫秒）。我回头一想，改了这么多，才减少了五六百毫秒，而这个广告页直接就
睡了一秒，啥也没干啊。我一想妈的也不能让你闲着，我要在这一秒里面做一些初始化工作，不能让他白闲着。所以就有了今天这个想法，我
要做一个智能的delay线程，然后在这个delay时间里面做一些初始化工作，我把这个初始化的工作分为两种。

###第一种情况：

    初始化的工作是主线程中的情况，这种情况也是最常规的一种情况。这种情况实现起来也很简单，但是，优化的时间并不多，最多也就是一二百毫秒而已。

####实现：

1 自定义一线程，将delay时间作为参数传入

2 实现init,和onNext的回调

3 在线程的run()方法里面，调用init,之后计算cost时间，之后根据init的时间来判断线程真正的sleep时间。

###第二种情况：

    需要做一些必要的网络请求操作才能实现跳转，这里注意，这些操作必须是必要操作，不做就没法使用后续功能的操作。我们也可以把这些操作抽离出来，动态计算一些耗时，之后根据耗时来判断后续睡眠时间。
