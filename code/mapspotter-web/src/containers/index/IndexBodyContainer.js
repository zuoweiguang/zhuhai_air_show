import React, { Component } from 'react'
import {Link} from 'react-router'
import '../../scss/index/indexBody.scss'
import {APP_ROOT_NAME} from '../../config/appConfig'

export default class IndexBodyContainer extends Component {
    constructor(props, context) {
        super(props, context)
    }

    render() {
        const bodyHeight = document.body.clientHeight - 80;
        const bodyStyle = {
            height: bodyHeight + 'px'
        }
        return (
            <div className="index-body" style={bodyStyle}>
                <div className="bg-box" style={{background:'url("app/images/index/banner.png") no-repeat center'}}>
                    <div className="desc-box">
                        <p className="desc-zh">MapSpotter 大数据平台</p>
                        <p className="desc-en">Running Your Data on Map</p>
                    </div>
                    <div className="btn-box">
                        <Link to={APP_ROOT_NAME+"studio"}>立即体验</Link>
                    </div>
                </div>
            </div>
        )
    }
}
