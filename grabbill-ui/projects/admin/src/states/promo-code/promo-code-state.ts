import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  ApiResponseModel,
  makePageable,
  makeSearchResultPayload,
  PromoCodeBasicModel,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { PromoCodeStateModel } from './promo-code-state.model';
import { PromoCodeManagementApi } from '../../api/promo-code-management.api';
import {
  DeletePromoCode,
  NewPromoCode,
  QueryPromoCodes,
  ResetPromoCodes,
  UpdatePromoCode,
  UpdatePromoCodeStatus,
} from './promo-code.state-actions';

@State<PromoCodeStateModel>({
  name: 'promo_code',
  defaults: {
    promoCodePageable: makePageable(10),
    promoCodeSearchResult: makeSearchResultPayload(),
  },
})
@Injectable()
export class PromoCodeState {
  constructor(private promoCodeManagementApi: PromoCodeManagementApi) {}

  @Selector()
  static promoCodeSearchResult(state: PromoCodeStateModel) {
    return state.promoCodeSearchResult;
  }

  @Selector()
  static promoCodePageable(state: PromoCodeStateModel) {
    return state.promoCodePageable;
  }

  @Selector()
  static promoCode(state: PromoCodeStateModel) {
    return state.promoCode;
  }

  @Action(ResetPromoCodes)
  resetPromoCodes(context: StateContext<PromoCodeStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.promoCodeSearchResult = makeSearchResultPayload();
        draft.promoCodePageable = makePageable(10);
        draft.code = undefined;
      })
    );
  }

  @Action(QueryPromoCodes)
  queryPromoCodes(context: StateContext<PromoCodeStateModel>, { pageable, code }: QueryPromoCodes) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.promoCodePageable = pageable ? pageable : draft.promoCodePageable;
        draft.code = code !== undefined ? code : draft.code;
      })
    );

    return this.promoCodeManagementApi
      .searchPromoCodes(context.getState().promoCodePageable, context.getState().code)
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<PromoCodeBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.promoCodeSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(NewPromoCode)
  newPromoCode(context: StateContext<PromoCodeStateModel>, { request }: NewPromoCode) {
    return this.promoCodeManagementApi.newPromoCode(request).pipe(tap(() => context.dispatch(new QueryPromoCodes())));
  }

  @Action(UpdatePromoCode)
  updatePromoCode(context: StateContext<PromoCodeStateModel>, { id, request }: UpdatePromoCode) {
    return this.promoCodeManagementApi
      .updatePromoCode(id, request)
      .pipe(tap(() => context.dispatch(new QueryPromoCodes())));
  }

  @Action(UpdatePromoCodeStatus)
  updatePromoCodeStatus(context: StateContext<PromoCodeStateModel>, { id, request }: UpdatePromoCodeStatus) {
    return this.promoCodeManagementApi
      .setPromoCodeStatus(id, request)
      .pipe(tap(() => context.dispatch(new QueryPromoCodes())));
  }

  @Action(DeletePromoCode)
  deletePromoCode(context: StateContext<PromoCodeStateModel>, { id }: DeletePromoCode) {
    return this.promoCodeManagementApi.deletePromoCode(id).pipe(tap(() => context.dispatch(new QueryPromoCodes())));
  }
}
