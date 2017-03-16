#!/usr/bin/perl

# This script exists because the newer NWS icons don't seem to be
# zipped up or easily available anywhere
# It takes as input a directory containing the older NWS icons, which I retrieved from
# http://w1.weather.gov/images/fcicons/Weather%20Icons.zip
# It uses the names of the JPG files in this directory to determine the PNG filenames to search for
# It then downloads all of the existing files with these names from the new icon URLs
# Currently, there seem to be 2 URLS, one for medium and one for large images

use strict;
use warnings;
use File::Path qw(make_path);
use LWP::UserAgent;

my ($iconNameDirectory, $destDirectory) = @ARGV;

die "$0 - retrieve the new (2015) NWS forecast icons, with the icon names determined by searching through a directory of the old icons" .
"\nUsage - $0 iconNameDirectory destDirectory" .
"\niconNameDirectory - directory containing the old NWS icons (these should be in JPEG format)" .
"\ndestDirectory - directory to save the new NWS icons (these will be in PNG format)\n" unless
defined $iconNameDirectory and defined $destDirectory;

my $linkPrefix = 'http://forecast.weather.gov/newimages/';
my @sizes = ('medium', 'large');
my @prefixes = ('', 'n');
my @suffixes = ('', '_ovc', '_bkn', '_sct', '_few');
my $newLinkSuffix = '.png';
my $oldLinkSuffix = '.jpg';

my $lwp = LWP::UserAgent->new(agent=>' Mozilla/5.0 (Windows NT 6.1; WOW64; rv:24.0) Gecko/20100101 Firefox/24.0', cookie_jar=>{});

die "Error: iconNameDirectory '$iconNameDirectory' does not exist\n" unless -d $iconNameDirectory;

if (not -d $destDirectory) {
    make_path($destDirectory) or die "Error: destDirectory '$destDirectory' does not exist and cannot be created";
}

my @fileNames;
my @filePaths = <"$iconNameDirectory/*$oldLinkSuffix">;

foreach my $filePath (@filePaths) {
    my $fileName = $filePath;

    # Remove up to and including the last forward slash
    $fileName =~ s/(.*)\///;

    # Remove the file suffix
    my $find = "\\$oldLinkSuffix";
    my $replace = "";
    $fileName =~ s/$find/$replace/;

    push @fileNames, $fileName;
}

foreach my $size (@sizes) {
    foreach my $prefix (@prefixes) {
        foreach my $suffix (@suffixes) {
            foreach my $fileName (@fileNames) {
                my $filePrefix = '';
                my $fileSuffix = '';

                if (length($prefix) == 0 || $fileName !~ /^\Q$prefix/) {
                    $filePrefix = $prefix;
                }

                if (length($suffix) == 0 || $fileName !~ /\Q$suffix$/) {
                    $fileSuffix = $suffix;
                }

                my $link = "$linkPrefix$size/$filePrefix$fileName$fileSuffix$newLinkSuffix";

                my $response = $lwp->mirror($link, "$destDirectory/$size" . "_" . "$filePrefix$fileName$fileSuffix$newLinkSuffix");

                unless ($response->is_success) {
                    print "Failed to download '$link': " . $response->status_line . "\n";
                }
            }
        }
    }
}
