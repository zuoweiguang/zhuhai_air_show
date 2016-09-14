import React, {Component, PropTypes} from "react";

export default class HelpBox extends Component {
    constructor(props, context) {
        super(props, context)
    }

    componentDidMount() {
        this.props.studioActions.studioMenuLinkAction('help')
    }


    render() {
        const {studioActions} = this.props
        return (
            <div className="helpinfo-box">
                <center>
                    <div className="content-containter content-center">
                        <div className="content-info-icon content-info-tips">
                        </div>
                        <div className="content-info-label">
                            <p className="label-title">温馨提示：</p>
                            <p className="label-desc">建议使用Chrome浏览器，操作效果更佳~ </p>
                        </div>


                    </div>
                    <div className="content-containter">
                        <div className="content-info-icon content-info-mail" >
                        </div>
                        <div className="content-info-label" >
                            <p className="label-title">联系我们：</p>
                            <p className="label-desc">
                                欢迎发送邮件至：
                                <a href="mailto:zhanghaiyan@cennavi.com.cn">zhanghaiyan@cennavi.com.cn
                                </a>
                                ,我们会第一时间给您回复！
                            </p>
                        </div>

                    </div>
                </center>
            </div>
        )
    }
}

HelpBox.propTypes = {
    studioActions: PropTypes.object.isRequired
}