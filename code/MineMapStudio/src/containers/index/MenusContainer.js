import React, { Component, PropTypes } from 'react'
import {MenuLogoPanel} from '../../components/index/MenuLogoPanel'
import {MenuPanel} from '../../components/index/MenuPanel'
import {MenuRightPanel} from '../../components/index/MenuRightPanel'

export default class MenusContainer extends Component {
    constructor(props, context) {
        super(props, context)
    }

    render() {
        const { indexMenu,user,menuActions,userActions} = this.props
        return (
            <div className="container">
                <div className="navbar-header">
                    <MenuLogoPanel />
                    <MenuPanel links = {indexMenu.indexMenuLinks} actions={menuActions}/>
                    <MenuRightPanel user = {user} actions={userActions}/>
                </div>
            </div>
        )
    }
}

MenusContainer.propTypes = {
    indexMenu: PropTypes.object.isRequired,
    user: PropTypes.object.isRequired,
    menuActions: PropTypes.object.isRequired,
    userActions: PropTypes.object.isRequired,
}

