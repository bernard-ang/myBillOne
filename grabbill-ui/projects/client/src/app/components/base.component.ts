import { Directive, OnChanges, OnDestroy } from '@angular/core';
import { Observable, Subscription } from 'rxjs';

@Directive()
export abstract class BaseComponent implements OnDestroy, OnChanges {
  protected _subs: Subscription[] = [];
  protected _onChangesSubs: Subscription[] = [];

  autoUnsubscribe(...subs: (Subscription | Observable<unknown>)[]) {
    subs.map((sub) => {
      if (sub instanceof Subscription) {
        this._subs.push(sub);
      } else {
        this._subs.push(sub.subscribe());
      }
    });
  }

  autoUnsubscribeOnChanges(...subs: (Subscription | Observable<unknown>)[]) {
    subs.map((sub) => {
      if (sub instanceof Subscription) {
        this._onChangesSubs.push(sub);
      } else {
        this._onChangesSubs.push(sub.subscribe());
      }
    });
  }

  ngOnChanges(): void {
    if (this._onChangesSubs.length > 0) {
      this._onChangesSubs.map((sub) => sub.unsubscribe());
      this._onChangesSubs.length = 0;
    }
  }

  ngOnDestroy() {
    if (this._subs.length > 0) {
      this._subs.map((sub) => sub.unsubscribe());
      this._subs.length = 0;
    }
  }
}
