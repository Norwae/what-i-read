package rate

import (
	ae "appengine"
	mc "appengine/memcache"
	"errors"
	"time"
)

type RateLimited struct {
	context ae.Context
	rate    time.Duration
}

var (
	ErrOverQuota error = errors.New("Function not executed: Over quota")
	ErrTimeout   error = errors.New("Timeout exceeded waiting for execution slice")
)

func New(ctx ae.Context, rate time.Duration) *RateLimited {
	return &RateLimited{ctx, rate}
}

func (l *RateLimited) Run(key string, f func()) error {
	item := mc.Item{
		Key:        key,
		Value:      []byte{0xff},
		Expiration: l.rate,
	}

	if err := mc.Add(l.context, &item); err != mc.ErrNotStored {
		l.context.Debugf("Executing function (err: %v)", err)
		f()
		return nil
	} else {
		l.context.Infof("Rejected function (err: %v)", err)
		return ErrOverQuota
	}

}

func (l *RateLimited) Queue(key string, timeout time.Duration, f func()) error {
	abort := time.NewTimer(timeout)
	retries := time.NewTicker(l.rate)

	defer abort.Stop()
	defer retries.Stop()

	for err := l.Run(key, f); err == ErrOverQuota; err = l.Run(key, f) {
		select {
		case <-retries.C: // no op
		case <-abort.C:
			return ErrTimeout
		}
	}

	return nil
}
