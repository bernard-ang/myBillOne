import produce from 'immer';
import { Injectable } from '@angular/core';
import { tap } from 'rxjs';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import {
  AffiliateCodeBasicModel,
  ApiResponseModel,
  makePageable,
  makeSearchResultPayload,
  SearchResultPayloadModel,
} from '@grabbill/lib';
import { AffiliateCodeStateModel } from './affiliate-code-state.model';
import { AffiliateCodeManagementApi } from '../../api/affiliate-code-management.api';
import {
  DeleteAffiliateCode,
  GenerateMasterCode,
  NewAffiliateCode,
  QueryAffiliateCodes,
  ResetAffiliateCodes,
  UpdateAffiliateCode,
} from './affiliate-code.state-actions';

@State<AffiliateCodeStateModel>({
  name: 'affiliate_code',
  defaults: {
    affiliateCodePageable: makePageable(10),
    affiliateCodeSearchResult: makeSearchResultPayload(),
  },
})
@Injectable()
export class AffiliateCodeState {
  constructor(private affiliateCodeManagementApi: AffiliateCodeManagementApi) {}

  @Selector()
  static affiliateCodeSearchResult(state: AffiliateCodeStateModel) {
    return state.affiliateCodeSearchResult;
  }

  @Selector()
  static affiliateCodePageable(state: AffiliateCodeStateModel) {
    return state.affiliateCodePageable;
  }

  @Selector()
  static newMasterCode(state: AffiliateCodeStateModel) {
    return state.newMasterCode;
  }

  @Action(ResetAffiliateCodes)
  resetAffiliateCodes(context: StateContext<AffiliateCodeStateModel>) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.affiliateCodeSearchResult = makeSearchResultPayload();
        draft.affiliateCodePageable = makePageable(10);
        draft.code = undefined;
      })
    );
  }

  @Action(QueryAffiliateCodes)
  queryAffiliateCodes(context: StateContext<AffiliateCodeStateModel>, { pageable, code }: QueryAffiliateCodes) {
    context.setState(
      produce(context.getState(), (draft) => {
        draft.affiliateCodePageable = pageable ? pageable : draft.affiliateCodePageable;
        draft.code = code !== undefined ? code : draft.code;
      })
    );

    return this.affiliateCodeManagementApi
      .getAffiliateCodes(context.getState().affiliateCodePageable, context.getState().code)
      .pipe(
        tap((response: ApiResponseModel<SearchResultPayloadModel<AffiliateCodeBasicModel>>) => {
          context.setState(
            produce(context.getState(), (draft) => {
              draft.affiliateCodeSearchResult = response.data;
            })
          );
        })
      );
  }

  @Action(NewAffiliateCode)
  newAffiliateCode(context: StateContext<AffiliateCodeStateModel>, { request }: NewAffiliateCode) {
    return this.affiliateCodeManagementApi
      .newAffiliateCode(request)
      .pipe(tap(() => context.dispatch(new QueryAffiliateCodes())));
  }

  @Action(UpdateAffiliateCode)
  updateAffiliateCode(context: StateContext<AffiliateCodeStateModel>, { id, request }: UpdateAffiliateCode) {
    return this.affiliateCodeManagementApi
      .updateAffiliateCode(id, request)
      .pipe(tap(() => context.dispatch(new QueryAffiliateCodes())));
  }

  @Action(DeleteAffiliateCode)
  deleteAffiliateCode(context: StateContext<AffiliateCodeStateModel>, { id }: DeleteAffiliateCode) {
    return this.affiliateCodeManagementApi
      .deleteAffiliateCode(id)
      .pipe(tap(() => context.dispatch(new QueryAffiliateCodes())));
  }

  @Action(GenerateMasterCode)
  generateMasterCode(context: StateContext<AffiliateCodeStateModel>) {
    return this.affiliateCodeManagementApi.generateMasterCode().pipe(
      tap((response) => {
        context.setState(
          produce(context.getState(), (draft) => {
            draft.newMasterCode = response.data.code;
          })
        );
      })
    );
  }
}
