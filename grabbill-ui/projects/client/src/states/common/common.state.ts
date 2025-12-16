import { Action, Selector, State, StateContext } from '@ngxs/store';
import { Injectable } from '@angular/core';
import { CommonStateModel } from './common.state-model';
import {
  ResetMessage,
  SetPageLoading,
  ShowMessage,
  UpdateBreadcrumb,
  UpdateSectionTitle,
} from './common.state-actions';

@State<CommonStateModel>({
  name: 'common',
  defaults: {
    sectionTitle: 'Home',
    breadcrumbs: [],
    pageLoading: false,
  },
})
@Injectable()
export class CommonState {
  @Selector()
  static message(state: CommonStateModel) {
    return state.message;
  }

  @Selector()
  static sectionTitle(state: CommonStateModel) {
    return state.sectionTitle;
  }

  @Selector()
  static crumbs(state: CommonStateModel) {
    return state.breadcrumbs;
  }

  @Selector()
  static pageLoading(state: CommonStateModel) {
    return state.pageLoading;
  }

  @Action(ShowMessage)
  showMessage(context: StateContext<CommonStateModel>, { messageType, message }: ShowMessage) {
    context.patchState({ message: { message, messageType } });
  }

  @Action(ResetMessage)
  resetMessage(context: StateContext<CommonStateModel>) {
    context.patchState({ message: undefined });
  }

  @Action(UpdateSectionTitle)
  updateSectionTitle(context: StateContext<CommonStateModel>, { sectionTitle }: UpdateSectionTitle) {
    context.patchState({ sectionTitle: sectionTitle });
  }

  @Action(UpdateBreadcrumb)
  updateBreadcrumb(context: StateContext<CommonStateModel>, { breadcrumbs }: UpdateBreadcrumb) {
    context.patchState({ breadcrumbs: breadcrumbs });
  }

  @Action(SetPageLoading)
  setPageLoading(context: StateContext<CommonStateModel>, { loading }: SetPageLoading) {
    context.patchState({ pageLoading: loading });
  }
}
