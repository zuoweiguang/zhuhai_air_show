import React, { Component, PropTypes } from 'react'
import '../../scss/operate/operateHelp.scss'

export default class HelpBox extends Component {
    constructor(props, context) {
        super(props, context)
    }

    render() {
        return <div className="operate-help-box-container">
            <div className="operate-help-header">
                <div className="title">帮助</div>
            </div>
            <div className="operate-help-content-body">
                <div className="fs16">操作台各按钮功能介绍如下：</div>
                <div className="content-main">1.地图操作键</div>
                <div className="content-sub">
                    <img className="sub-img" src="/app/images/operate/help_keys.png"/>
                </div>
                <div className="content-sub">从左到右功能依次为：<br/>
                    逆时针旋转、向上移动、顺时针旋转、减小俯角、<br/>
                    向左移动、向下移动、向右移动、增加俯角
                </div>
                <div className="content-main">2.视角切换键</div>
                <div className="content-sub">
                    <img className="sm-img" src="/app/images/operate/2d.png"/>&nbsp;&#92;&nbsp;
                    <img className="sm-img" src="/app/images/operate/3d.png"/>&nbsp;点击，实现平面与立体建筑效果的实现。
                </div>
                <div className="content-main">3.快捷键操作</div>
                <div className="content-sub">
                    Shift + A （或B~H、P）：地图动画效果移动。<br/>
                    ↑&nbsp;&#92;&nbsp;↓&nbsp;&#92;&nbsp;←&nbsp;&#92;&nbsp;→：移动地图。<br/>
                    &lt;&nbsp; &#92;&nbsp; &gt; ：缩小或放大地图显示级别 1级。<br/>
                    &#123;或[&nbsp;&#92;&nbsp;&#125;或]：俯视或仰视地图5度。<br/>
                    - &nbsp;&#92;&nbsp;+: 逆时针或顺时针旋转地图25度。<br/>
                </div>
            </div>
        </div>
    }
}
