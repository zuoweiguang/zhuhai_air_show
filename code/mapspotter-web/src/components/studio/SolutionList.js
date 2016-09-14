import React, { Component, PropTypes } from 'react'
import SolutionPanel from './SolutionPanel'
import SolutionAddPanel from './SolutionAddPanel'
import UserTool from '../../utils/UserTool'

export default class SolutionList extends Component {
    constructor(props, context) {
        super(props, context)
    }

    componentDidMount() {
        this.props.actions.solutionListAction(UserTool.getLoginUserId())
        this.props.studioActions.studioMenuLinkAction('solution')
    }

    render() {
        const { solutions, templates,actions,studioActions} = this.props
        const solutionPanels = solutions.map((solution, i) => {
            return <div className="col-sm-6 col-md-6" key={solution.id}><SolutionPanel
                solution={solution} {...actions}/></div>
        })
        return <div className="container-fluid solution-list">
            <SolutionAddPanel templates={templates} solutionAddAction={actions.solutionAddAction}
                              solutionTemplateSelectAction={actions.solutionTemplateSelectAction}
                              solutionTemplateListAction={actions.solutionTemplateListAction}/>
            {solutionPanels}
        </div>
    }
}

SolutionList.propTypes = {
    solutions: PropTypes.array.isRequired,
    templates: PropTypes.array.isRequired,
    actions: PropTypes.object.isRequired
}