import React, { Component, PropTypes } from 'react'
import {Link} from 'react-router'

export class MenuLink extends Component {
    constructor(props, context) {
        super(props, context)
    }
    render() {
        const { link,indexMenuClickAction} = this.props
        return <Link key={link.id} to={link.href} activeClassName={link.selected} onClick={() => indexMenuClickAction(link.id)}>{link.name}</Link>
    }
}

MenuLink.propTypes = {
    link: PropTypes.object.isRequired
}

export class MenuPanel extends Component {
    constructor(props, context) {
        super(props, context)
    }
    render() {
        const { links, actions} = this.props
        const menus = links.map(link => <MenuLink key={link.id} link={link} {...actions}/>)
        return <div className="header-menu">{menus}</div>
    }
}

MenuPanel.propTypes = {
    links: PropTypes.array.isRequired,
    actions: PropTypes.object.isRequired
}

