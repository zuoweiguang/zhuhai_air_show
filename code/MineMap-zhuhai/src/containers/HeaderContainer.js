import React, { Component, PropTypes } from 'react'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import '../scss/index/header.css'
import * as AuthActions from '../actions/AuthAction'
import {MenuLogoPanel} from '../components/index/MenuLogoPanel'
class HeaderContainer extends Component {
    constructor(props, context) {
        super(props, context)
    }

    render() {

        return (
            <div className="page-header-container">
                <MenuLogoPanel />
            </div>
        )
    }
}

export default HeaderContainer
