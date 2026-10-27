# Shared by home-take and home-restore. See docs/adr/0001-home-takeover.md for why these packages.
# shellcheck disable=SC2034  # used by the scripts that source this file

KIMONO=io.github.ranjandas.kimono
STOCK_HOME=com.google.android.apps.tv.launcherx
STOCK_HOME_ACTIVITY=$STOCK_HOME/.home.HomeActivity
# Takes over Home with RecoveryActivity, in a relaunch loop, once the stock launcher is off.
SETUP_HELPER=com.google.android.tungsten.setupwraith

DRY_RUN=0

# Set ADB_SERIAL (e.g. 192.168.4.129:5555) when more than one device is connected.
adb_() {
    if [ -n "${ADB_SERIAL:-}" ]; then
        command adb -s "$ADB_SERIAL" "$@"
    else
        command adb "$@"
    fi
}

# Runs a command that changes the device, or only prints it with --dry-run.
change() {
    echo "+ adb $*"
    if [ "$DRY_RUN" -eq 0 ]; then
        adb_ "$@"
    fi
}

die() {
    echo "error: $*" >&2
    exit 1
}

require_device() {
    command -v adb >/dev/null || die "adb isn't on PATH"
    local state
    state=$(adb_ get-state 2>/dev/null) || die "no device; connect with adb connect <tv-ip>:<port>, or set ADB_SERIAL"
    [ "$state" = device ] || die "device is $state, not ready"
}

current_home() {
    adb_ shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME |
        tail -n 1 | tr -d '\r'
}

home_role_holder() {
    adb_ shell cmd role get-role-holders android.app.role.HOME | tr -d '\r'
}

show_state() {
    echo "Home opens:      $(current_home)"
    echo "HOME role:       $(home_role_holder)"
    echo "Disabled:        $(adb_ shell pm list packages -d | tr -d '\r' | grep -E "$STOCK_HOME|$SETUP_HELPER" |
        sed 's/^package://' | tr '\n' ' ')"
}
