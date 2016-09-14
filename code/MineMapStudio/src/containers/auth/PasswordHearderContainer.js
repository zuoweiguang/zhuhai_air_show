import React, { Component, PropTypes } from 'react'
import '../../scss/auth/login.scss'

import {MenuLogoPanel} from '../../components/index/MenuLogoPanel'
export default class PasswordHearderContainer extends Component {
    constructor(props, context) {
        super(props, context)

    }

    render() {
        return (
            <div className="pasword-header-container">
                <MenuLogoPanel />
            </div>
        )
    }
}
