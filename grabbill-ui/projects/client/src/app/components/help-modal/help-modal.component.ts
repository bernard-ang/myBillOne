import { ChangeDetectionStrategy, ChangeDetectorRef, Component } from '@angular/core';
import { MarkdownService } from 'ngx-markdown';
import { BaseComponent } from '../base.component';
import { of, switchMap, tap } from 'rxjs';
import { ActionCompletion, Actions, ofActionCompleted, Store } from '@ngxs/store';
import { CloseContextSensitiveHelp, OpenContextSensitiveHelp } from '../../../states/common/common.state-actions';
import { catchError } from 'rxjs/operators';
import { AuthState } from '../../../states/auth/auth.state';
import { MarkGuidedStepsViewed } from '../../../states/profile/profile.state-actions';

@Component({
  selector: 'grabbill-client-help-modal',
  templateUrl: './help-modal.component.html',
  styleUrls: ['./help-modal.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HelpModalComponent extends BaseComponent {
  isVisible = false;

  markdown = '';

  videoUrl?: string;

  constructor(
    private markdownService: MarkdownService,
    private store: Store,
    private actions$: Actions,
    private cd: ChangeDetectorRef
  ) {
    super();
  }

  ngOnInit(): void {
    this.autoUnsubscribe(
      this.actions$.pipe(
        ofActionCompleted(CloseContextSensitiveHelp),
        tap(() => {
          this.isVisible = false;
          this.cd.markForCheck();
        })
      ),
      this.actions$.pipe(
        ofActionCompleted(OpenContextSensitiveHelp),
        switchMap((data: ActionCompletion) => {
          const route = data.action.route;

          let src = 'assets/help/';
          console.log(route);
          if (route === '/') {
            src += 'dashboard.md';
          } else if (route.includes('/digital-filing')) {
            src += 'digital-filing.md';
          } else if (route.includes('/transactional-email')) {
            src += 'transactional-email.md';
          } else if (route.includes('/email-campaign')) {
            src += 'email-campaign.md';
          } else {
            src += 'grabbill.md';
          }

          return this.markdownService.getSource(src).pipe(
            tap((markdown) => {
              this.videoUrl = undefined;
              const result = markdown.match(/iframe:.+/);
              if (result) {
                this.videoUrl = result[0].replace('iframe:', '');
                this.markdown = markdown.replace(result[0], '');
              } else {
                this.markdown = markdown;
              }
              this.isVisible = true;
              const user = this.store.selectSnapshot(AuthState.user);
              if (!user?.guidedStepsViewed) {
                this.store.dispatch(new MarkGuidedStepsViewed());
              }
              this.cd.markForCheck();
            }),
            catchError((error) => {
              console.error(error);
              return of(null);
            })
          );
        })
      )
    );
  }

  doHelpClose() {
    this.store.dispatch(new CloseContextSensitiveHelp());
  }
}
