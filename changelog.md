# 1.1.8 Changelog

## New

No new features.

## Bug fixes / Changes

- Fixed bug where users with large amount of waypoints / groups would disconnect due to default size limit of 32k bytes.
    - Now fixed by increasing the limit to 2MB (2,097,000)
    - This is technically possible to reach but, I just refuse to believe anyone has that amount.

This fix is courtesy of isHaack on GitHub (resolves https://github.com/NavidRohim/JMWS/issues/4)

## Bugs that I am aware of

None!

### Other
*Server version is 1.01, fully backwards compatible with 1.00 granting you don't have a lot of waypoints / groups (100+)*