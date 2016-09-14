import React, { Component, PropTypes } from 'react'
import '../../scss/index/header.css'
import {MenuLogoPanel} from '../../components/index/MenuLogoPanel'

export default class LoginHeaderContainer extends Component {
    constructor(props, context) {
        super(props, context)
    }

    render() {
        const { indexMenu,user,menuActions,userActions} = this.props
        return (
            <div className="page-header-container">
                <div className="container">
                    <div className="navbar-header">
                        <MenuLogoPanel />
                        <div className="header-menu" style={{left:'200px'}}><span
                            className="header-title">MapSpotter大数据平台</span>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}
