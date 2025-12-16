import { Action, Selector, State, StateContext } from '@ngxs/store';
import { Injectable } from '@angular/core';
import { AdminCommonStateModel } from './admin-common-state.model';
import {
  ResetMessage,
  SetPageLoading,
  ShowMessage,
  UpdateBreadcrumb,
  UpdateSectionTitle,
} from './admin-common.state-actions';

@State<AdminCommonStateModel>({
  name: 'admin_common',
  defaults: {
    pageLoading: false,
    sectionTitle: 'Home',
    breadcrumbs: [],
  },
})
@Injectable()
export class AdminCommonState {
  @Selector()
  static message(state: AdminCommonStateModel) {
    return state.message;
  }

  @Selector()
  static sectionTitle(state: AdminCommonStateModel) {
    return state.sectionTitle;
  }

  @Selector()
  static crumbs(state: AdminCommonStateModel) {
    return state.breadcrumbs;
  }

  @Selector()
  static pageLoading(state: AdminCommonStateModel) {
    return state.pageLoading;
  }

  @Action(ShowMessage)
  showMessage(context: StateContext<AdminCommonStateModel>, { messageType, message }: ShowMessage) {
    context.patchState({ message: { message, messageType } });
  }

  @Action(ResetMessage)
  resetMessage(context: StateContext<AdminCommonStateModel>) {
    context.patchState({ message: undefined });
  }

  @Action(UpdateSectionTitle)
  updateSectionTitle(context: StateContext<AdminCommonStateModel>, { sectionTitle }: UpdateSectionTitle) {
    context.patchState({ sectionTitle: sectionTitle });
  }

  @Action(UpdateBreadcrumb)
  updateBreadcrumb(context: StateContext<AdminCommonStateModel>, { breadcrumbs }: UpdateBreadcrumb) {
    context.patchState({ breadcrumbs: breadcrumbs });
  }

  @Action(SetPageLoading)
  setPageLoading(context: StateContext<AdminCommonStateModel>, { loading }: SetPageLoading) {
    context.patchState({ pageLoading: loading });
  }
}
