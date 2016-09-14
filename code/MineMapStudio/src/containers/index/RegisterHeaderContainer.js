import React, { Component, PropTypes } from 'react'
import '../../scss/index/register.scss'

import {MenuLogoPanel} from '../../components/index/MenuLogoPanel'
export default class RegisterHeaderContainer extends Component {
    constructor(props, context) {
        super(props, context)

    }

    render() {
        return (
            <div className="register-header-container">
                <MenuLogoPanel />
            </div>
        )
    }
}

