import React, { Component, PropTypes } from 'react'
import {Link} from 'react-router'
import {APP_ROOT_NAME} from '../../config/appConfig'

export default class SolutionHeader extends Component {
    constructor(props, context) {
        super(props, context)
    }

    render() {
        const { indexMenu,user,menuActions,userActions} = this.props
        const {indexMenuClickAction} = menuActions
        return (
            <div className="studio-navbar-header">
                <Link
                    to={APP_ROOT_NAME} onClick={() => indexMenuClickAction('index')} title="返回首页">
                    <div className="back-box">
                        <div className="icon-back"/>
                    </div>
                </Link>
                <div className="content-box">
                    <span className="content-info">欢迎来到MapSpotter工作台</span>
                </div>
            </div>
        )
    }
}

SolutionHeader.propTypes = {
    indexMenu: PropTypes.object.isRequired,
    user: PropTypes.object.isRequired,
    menuActions: PropTypes.object.isRequired,
    userActions: PropTypes.object.isRequired,
}

