import React, { Component, PropTypes } from 'react'
import {browserHistory} from 'react-router'
import {APP_ROOT_NAME} from '../../config/appConfig'


export default class RegisterSuccessPanel extends Component {
    constructor(props, context) {
        super(props, context)
        this.state = {transformInfo: 3}
        this.showTime = this.showTime.bind(this)
        this.updataTime = this.updataTime.bind(this)

    }

    componentDidMount() {
        this.showTime()

    }

    updataTime(num) {
        this.setState({transformInfo: num})
        if (num == 0) {
            browserHistory.push({APP_ROOT_NAME})
            this.props.actions.RegisterBodyShowAction(true)
        }
    }

    showTime() {
        let secs = 3
        let updateTimeValue = setInterval(()=> {
            if (secs >= 0) {
                this.updataTime(secs)
                secs--
            }
            else {
                clearInterval(updateTimeValue)
            }
        }, 1000)
    }


    render() {
        const {user,actions} = this.props
        const bodyHeight = document.body.clientHeight - 80;
        const bodyStyle = {
            height: bodyHeight + 'px'
        }
        return (
            <div className="regsuccess-big-box" style={bodyStyle}>
                <div className="regsuccess-box">
                    <div className="regsuccessimg"><img src="/app/images/index/Correct.png" alt="some_text"
                                                        width="140"/></div>
                    <div className="regsuccesstext">
                        <div className="congratu">恭喜您，已经注册成功！</div>
                        <div className="timewaite">自动为您跳转页面，请稍等<span>{this.state.transformInfo}</span>...</div>
                    </div>
                </div>
            </div>
        )
    }
}
