package isbn13

import (
	"errors"
	"strconv"
)

const divisorStart = 1000000000000

type ISBN13 uint64

var invalid error = errors.New("Invalid ISBN 13")

func New(raw string) (ISBN13, error) {
	intVal, err := strconv.ParseUint(raw, 0, 64)

	if err == nil {
		if !Validate(intVal) {
			err = invalid
		}
	}

	return ISBN13(intVal), err
}

func Validate(value uint64) bool {
	sum := 0
	divisor := uint64(divisorStart)
	even := -1

	for i := 0; i < 13; i++ {
		part := (value / divisor) % 10
		mult := 2 + even

		sum += int(part) * mult
		even = ^even + 1
		divisor /= 10
	}

	return (sum % 10) == 0
}

func CheckDigit(isbn ISBN13) uint8 {
	return uint8(isbn % 10)
}

func (isbn ISBN13) String() string {
	return strconv.FormatUint(uint64(isbn), 10)
}

func (isbn ISBN13) KeyString(context string) string {
	return context + ":" + isbn.String()
}
