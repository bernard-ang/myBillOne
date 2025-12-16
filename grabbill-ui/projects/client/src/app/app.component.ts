import { Component, OnDestroy, OnInit } from '@angular/core';
import {
  NavigationCancel,
  NavigationEnd,
  NavigationError,
  NavigationStart,
  Router,
  RouterEvent,
} from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { Select, Store } from '@ngxs/store';
import { CommonState } from '../states/common/common.state';
import { SetPageLoading } from '../states/common/common.state-actions';
import buildInfo from '../../../../buildinfo.json';

@Component({
  selector: 'grabbill-client-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.less'],
})
export class AppComponent implements OnInit, OnDestroy {
  @Select(CommonState.pageLoading)
  pageLoading$!: Observable<boolean>;

  loading = false;
  loadingSubscription!: Subscription;
  routerEventSubscription!: Subscription;

  constructor(private router: Router, private store: Store) {}

  ngOnInit(): void {
    console.log('client', buildInfo.git);
    this.loadingSubscription = this.pageLoading$.subscribe((loading) => (this.loading = loading));
    this.routerEventSubscription = this.router.events.subscribe((event) => {
      this.navigationInterceptor(event as RouterEvent);
    });
  }

  ngOnDestroy(): void {
    this.routerEventSubscription.unsubscribe();
    this.loadingSubscription.unsubscribe();
  }

  navigationInterceptor(event: RouterEvent): void {
    if (event instanceof NavigationStart) {
      this.store.dispatch(new SetPageLoading(true));
    }
    if (event instanceof NavigationEnd) {
      this.store.dispatch(new SetPageLoading(false));
    }

    // Set loading state to false in both of the below events to hide the spinner in case a request fails
    if (event instanceof NavigationCancel) {
      this.store.dispatch(new SetPageLoading(false));
    }
    if (event instanceof NavigationError) {
      this.store.dispatch(new SetPageLoading(false));
    }
  }
}
