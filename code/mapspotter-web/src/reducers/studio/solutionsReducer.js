import {STUDIO_SOLUTION_LIST,STUDIO_SOLUTION_ADD,STUDIO_SOLUTION_DEL,STUDIO_SOLUTION_EDIT,STUDIO_SOLUTION_MODIFY,STUDIO_SOLUTION_COPY} from '../../constants/StudioActionTypes'
import { combineReducers } from 'redux'

function solutionsReducer(state = [], action) {
    switch (action.type) {
        case STUDIO_SOLUTION_LIST:
            return action.ready ? action.fetchData.solutions : state

        case STUDIO_SOLUTION_ADD:
            return [
                data,
                ...state
            ]
        case STUDIO_SOLUTION_DEL:
            return state.filter(solution =>
                solution.id !== action.id
            )

        case STUDIO_SOLUTION_EDIT:
            return state.map(solution =>
                solution.id === action.id ?
                    Object.assign({}, solution, {id: action.id}) :
                    solution
            )

        case STUDIO_SOLUTION_MODIFY:
            return state.map(solution =>
                solution.id === action.data.id ?
                    Object.assign({}, solution, action.data) :
                    solution
            )

        case STUDIO_SOLUTION_COPY:
            return [
                ...state, action.copySolution
            ]
        default:
            return state
    }
}

export default solutionsReducer